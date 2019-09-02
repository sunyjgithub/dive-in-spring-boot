package com.imooc.web.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.imooc.web.exception.BaseException;
import com.imooc.web.exception.CRUDException;
import com.imooc.web.req.BatchReq;
import com.imooc.web.req.MicroReq;
import com.imooc.web.resp.BaseResp;
import com.imooc.web.resp.BatchResp;
import com.imooc.web.resp.MicroResp;
import com.imooc.web.resp.ValidResult;
import com.imooc.web.util.JSONUtils;
import com.imooc.web.util.RespCodeUtils;
import com.imooc.web.util.ValidUtils;
import com.imooc.web.validation.Add;
import com.imooc.web.validation.Delete;
import com.imooc.web.validation.Update;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.groups.Default;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:26
 **/
@Aspect
@Component
public class CRUDRestAop {

    /**
     * LOG
     */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 注解元数据缓存<br>
     * 本AOP采用懒加载缓存方式，调用时将注解数据放入Map缓存起来，减少反射
     */
    private Map<String, Map<String, RestMeta>> metaMap = new ConcurrentHashMap<>();

    /**
     * 数据CUD切面增强
     * @param point 切面连接点
     * @exception JsonProcessingException json处理异常
     * @exception CRUDException 拦截处理异常
     */
    @Around("@annotation(com.imooc.web.aspect.CRUDRest)")
    public Object processCRUD(ProceedingJoinPoint point) throws JsonProcessingException, CRUDException, InstantiationException, IllegalAccessException {

        // 1.从缓存Map获取方法对应的注解元数据
        RestMeta thisMethodMeta = fetchCRUDMetaFromCache(point);

        // 2.根据不同的操作类型,想处理方法调用不同的参数
        switch (thisMethodMeta.getType()) {
            case Create:
                return cudProcess(point, thisMethodMeta, Add.class);
            case Update:
                return cudProcess(point, thisMethodMeta, Update.class);
            case Delete:
                return cudProcess(point, thisMethodMeta, Delete.class);
            case Default:
                return cudProcess(point, thisMethodMeta, Default.class);
            case Read:
                return query(point, thisMethodMeta);
            default:
                return null;
        }

    }

    /**
     * 获取元数据
     * @param point 切入点
     * @return 获取到的元数据
     */
    private RestMeta fetchCRUDMetaFromCache(ProceedingJoinPoint point) {
        // 1.1 开始获取class的元数据
        MethodSignature signature = (MethodSignature) point.getSignature();
        Class<? extends MethodSignature> pointClass = signature.getClass();
        String className = pointClass.getName();
        Map<String, RestMeta> thisCrudClassMetaMap = metaMap.get(className);
        if (thisCrudClassMetaMap == null) {
            thisCrudClassMetaMap = new ConcurrentHashMap<>();
            metaMap.put(className, thisCrudClassMetaMap);
        }

        // 1.2 开始获取method的元数据,并同class的元数据做合并
        Method method = signature.getMethod();
        RestMeta thisMethodMeta = thisCrudClassMetaMap.get(method.toGenericString());
        if (thisMethodMeta == null) {
            thisMethodMeta = compareCRUDMeta(signature);
            thisCrudClassMetaMap.put(method.toGenericString(), thisMethodMeta);
        }

        // 1.3 根据不同类型,赋值不同的提示语
        if (StringUtils.equals(CRUDRest._defaultSuccStr, thisMethodMeta.getSuccStr())) {
            thisMethodMeta.setSuccStr(CRUDRest._defaultSuccStrMap.get(thisMethodMeta.getType()));
        }

        // 1.4 返回
        return thisMethodMeta;
    }

    /**
     * 根据类、方法注解，比较并获取元数据
     * @param signature 被加强方法的签名
     * @return 元数据
     */
    private RestMeta compareCRUDMeta(MethodSignature signature) {
        // --pMethod--
        // 1.获取类的注解信息
        CRUDRest classCRUDRestAnnotation = (CRUDRest) signature.getDeclaringType().getAnnotation(CRUDRest.class);
        RestMeta classMeta = null;
        if (classCRUDRestAnnotation != null) {
            //将获取到的注解信息，封装到RestMeta对象中，RestMeta就是用来映射注解数据的
            classMeta = RestMeta.genMeta(classCRUDRestAnnotation);
        }

        // 2.获取方法的注解信息
        Method method = signature.getMethod();
        CRUDRest methodAnnotation = method.getAnnotation(CRUDRest.class);

        //将获取到的注解信息，封装到RestMeta对象中，RestMeta就是用来映射注解数据的
        RestMeta methodMeta = RestMeta.genMeta(methodAnnotation);

        // 3.比较方法和类的注解信息.方法注解优先于类注解
        //意思就是方法上有 就去方法上的值，如果没有获取类上的值
        String finalFaildStr = StringUtils.equals(CRUDRest._defaultFaildStr, methodMeta.getFaildStr())
                && (classMeta != null) ? classMeta.getFaildStr() : methodMeta.getFaildStr();
        String finalModelCode = StringUtils.equals(CRUDRest._defaultModelCode, methodMeta.getModelCode())
                && (classMeta != null) ? classMeta.getModelCode() : methodMeta.getModelCode();
        String finalSuccStr = StringUtils.equals(CRUDRest._defaultSuccStr, methodMeta.getSuccStr())
                && (classMeta != null) ? classMeta.getSuccStr() : methodMeta.getSuccStr();
        Class finalExceptionClass = CRUDRest._defaultException.equals(methodMeta.getException()) && (classMeta != null)
                ? classMeta.getException()
                : methodMeta.getException();
        // type只能作用到方法上,因此这里只能取方法的。
        CRUDRest.Type finalType = methodMeta.getType();

        // 4.装箱======RestMeta映射@CRUDRest注解属性值
        RestMeta finalMeta = new RestMeta();
        finalMeta.setFaildStr(finalFaildStr);
        finalMeta.setType(finalType);
        finalMeta.setSuccStr(finalSuccStr);
        finalMeta.setModelCode(finalModelCode);
        finalMeta.setException(finalExceptionClass);

        // 5. return
        return finalMeta;
        // --pMethod--
    }

    /**
     * 数据更新切面增强
     * @param point 切面连接点
     */
    private Object query(ProceedingJoinPoint point, RestMeta thisMethodMeta) throws IllegalAccessException, CRUDException, InstantiationException {

        String succString = thisMethodMeta.getSuccStr();
        String faildString = thisMethodMeta.getFaildStr();
        String modelCode = thisMethodMeta.getModelCode();

        BaseResp resp = null;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        try {
            Object result = point.proceed();
            resp = (BaseResp) result;
            resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__SUCC));
            resp.getMessage().setDescription(succString);
        } catch (Throwable e) {
            CRUDRest annotation = methodSignature.getMethod().getAnnotation(CRUDRest.class);
            Class exception = annotation.exception();
            resp = processQryException(point, faildString, modelCode, e, exception);
        }
        return resp;
    }

    /**
     * cudProcess 批量处理
     * @param point 切入点
     * @param thisMethodMeta 元数据信息
     * @param validGroup 校验组
     * @return 处理结果
     */
    @SuppressWarnings({ "rawtypes", "unused", "unchecked" })
    private Object cudProcess(ProceedingJoinPoint point, RestMeta thisMethodMeta, Class validGroup)
            throws JsonProcessingException, CRUDException, IllegalAccessException, InstantiationException {

        // 1.处理请求参数
        // 1.1 获取增加方法的参数
        Method method = fetchPointMethod(point);
        Parameter[] parameters = method.getParameters();

        // 1.2 找到批处理参数、要校验的参数
        Object requestBodyData = null;
        List<Object> newValidParameterLst = Lists.newArrayList(); // 要校验的参数列表
        for (int i = 0; i < parameters.length; i++) {

            // 获取requestBody报文体对象
            if (parameters[i].getAnnotation(RequestBody.class) != null) {  // isAssignableFrom TAG
                requestBodyData = point.getArgs()[i];
            }
            // 获取parameter对象参数
            if (parameters[i].getAnnotation(CURDValid.class) != null) {
                newValidParameterLst.add(point.getArgs()[i]);
            }
        }

        // 1.3 调用前参数校验及切点方法调用
        if (requestBodyData == null) {  // 如果没有requestBody报文体，进入不校验body的流程
            return NoBodyProcess(point, validGroup, thisMethodMeta, method, newValidParameterLst);
        } else if (requestBodyData instanceof MicroReq) {   // 对通用microReq controller的处理
            return microReqBodyProcess(point, validGroup, thisMethodMeta, (MicroReq<?>) requestBodyData, method,
                    newValidParameterLst);
        } else { // 对个性化requestBody的处理
            return customReqBodyProcess(point, validGroup, thisMethodMeta, requestBodyData, method,
                    newValidParameterLst);
        }

    }

    /**
     * 获取被切方法的method反射对象
     * @param point 被切点
     * @return  切点反射对象
     */
    private Method fetchPointMethod(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return signature.getMethod();
    }

    /**
     * 无request body的处理逻辑
     * @param point 切点
     * @param validGroup 校验组
     * @param method 切点方法
     * @return 处理结果Response
     */
    private Object NoBodyProcess(ProceedingJoinPoint point, Class validGroup, RestMeta thisMethodMeta, Method method,
                                 List<Object> newValidParameterLst) throws CRUDException, InstantiationException, IllegalAccessException {

        String succString = thisMethodMeta.getSuccStr();
        String faildString = thisMethodMeta.getFaildStr();
        String modelCode = thisMethodMeta.getModelCode();

        // 1.生成默认的应答
        BaseResp resp = genDefaultResp(method);

        try {
            if (!validParameterList(validGroup, thisMethodMeta, newValidParameterLst, resp)) {
                return resp;
            }
            resp = (BaseResp) point.proceed();
            if (StringUtils.isBlank(resp.getResultCode())) {
                resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__SUCC));
                if (StringUtils.isBlank(resp.getMessage().getDescription())) {
                    resp.getMessage().setDescription(succString);
                }
            }
            log.debug("CUD返回结果:{}", JSONUtils.getInstance().writeValueAsString(resp));
        } catch (Throwable e) {
            CRUDRest annotation = method.getAnnotation(CRUDRest.class);
            Class exception = annotation.exception();
            processCUDException(faildString, modelCode, resp, e, exception);
        }

        return resp;
    }

    /**
     * 非microReq的自定义Bean requestBody请求处理
     * @param point 切点
     * @param validGroup 校验组
     * @param thisMethodMeta 切点方法定义的元数据
     * @param requestBody 请求报问题
     * @param method 切点方法
     * @param validParameterLst 要校验的parameter参数
     * @return 处理结果Response
     */
    private Object customReqBodyProcess(ProceedingJoinPoint point, Class validGroup, RestMeta thisMethodMeta,
                                        Object requestBody, Method method, List<Object> validParameterLst)
            throws JsonProcessingException, CRUDException, InstantiationException, IllegalAccessException {

        // 1.准备数据
        // 1.1 生成默认的应答
        BaseResp resp = genDefaultResp(method);
        log.debug("操作入参数据 :{}", JSONUtils.getInstance().writeValueAsString(requestBody));
        String succString = thisMethodMeta.getSuccStr(); // 获取返回的成功提示语
        String faildString = thisMethodMeta.getFaildStr(); // 获取返回的失败提示语
        String modelCode = thisMethodMeta.getModelCode(); // 获取返回的模块代码

        try {

            // 2.校验其他非body参数
            if (!validParameterList(validGroup, thisMethodMeta, validParameterLst, resp)) {
                return resp;
            }

            // 3.从备份的原始消息数据中，遍历获取每条数据进行校验
            // 校验body中要处理数据集合
            ValidResult invalidMsg = ValidUtils.valid(requestBody, validGroup);
            if (invalidMsg != null) {
                resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INVALID_INPUT));
                resp.setMessage(invalidMsg);
                return resp;
            }

            // 4.开始进行业务处理
            resp = (BaseResp) point.proceed();
            if (StringUtils.isBlank(resp.getResultCode())) {
                resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__SUCC));
                if (StringUtils.isBlank(resp.getMessage().getDescription())) {
                    resp.getMessage().setDescription(succString);
                }
            }
        } catch (Throwable e) {
            CRUDRest annotation = method.getAnnotation(CRUDRest.class);
            Class exception = annotation.exception();
            processCUDException(faildString, modelCode, resp, e, exception);
        }

        log.debug("CUD返回结果:{}", JSONUtils.getInstance().writeValueAsString(resp));
        return resp;
    }

    /**
     * CUD批处理
     * @param point 切点
     * @param validGroup 校验组
     * @param thisMethodMeta 切点方法定义的元数据
     * @param microReq 批处理请求参数
     * @param method 切点方法
     * @param validParameterLst 要校验的parameter参数
     * @return 处理结果Response
     */
    private Object microReqBodyProcess(ProceedingJoinPoint point, Class validGroup, RestMeta thisMethodMeta,
                                       MicroReq<?> microReq, Method method, List<Object> validParameterLst)
            throws JsonProcessingException, CRUDException, InstantiationException, IllegalAccessException {

        // 1.准备数据
        List<BatchReq<?>> backAllReqData = Lists.newArrayList((List) microReq.getData());  // 获取原始请求的数据，并放置到一个新的List数组中。
        microReq.getData().clear(); // 原因是clear之前保存全部的带处理数据.将原始入参清空，调用被增强方法时，实际每次只传入了需要处理的数据

        // 1.1 生成默认的应答
        BaseResp resp = genDefaultResp(method);
        log.debug("操作入参数据 :{}", JSONUtils.getInstance().writeValueAsString(backAllReqData));

        // 2.校验其他非body参数
        if (!validParameterList(validGroup, thisMethodMeta, validParameterLst, resp)) {
            return resp;
        }

        if (microReq.isSingleTrans()) { // 所有数据在单事务中处理
            resp = singleTransProcess(point, validGroup, thisMethodMeta, microReq, method, backAllReqData);
        } else { // 所有数据在不同事务中处理
            resp = mulTransProcess(point, validGroup, thisMethodMeta, microReq, method, backAllReqData);
        }

        log.debug("CUD返回结果:{}", JSONUtils.getInstance().writeValueAsString(resp));
        return resp;
    }

    /**
     * 生成默认的应答消息对象。如果业务代码抛出异常时，用此对象返回结果。
     * @param method
     * @return
     * @throws CRUDException
     */
    private BaseResp genDefaultResp(Method method) throws CRUDException, IllegalAccessException, InstantiationException {
        Class<?> returnType = method.getReturnType();
        if (!BaseResp.class.isAssignableFrom(returnType)){
            //这个方法决定了 返回值必须继承自BaseResp类型
            throw new CRUDException("使用了无效的微服务response类型,务必继承自BaseResp类型!");
        }
        return (BaseResp) returnType.newInstance();
    }

    /**
     * 校验request的parameter参数
     * @param validGroup 校验组
     * @param thisMethodMeta 当前切点control方法的元数据信息
     * @param validParameterLst 被校验参数集合
     * @param microResp response对象，如果校验失败，则reponse会被赋值
     * @return true 校验通过 false 校验未通过
     */
    private boolean validParameterList(Class validGroup, RestMeta thisMethodMeta, List<Object> validParameterLst,
                                       BaseResp microResp) {
        for (Object toValidPar : validParameterLst) {
            ValidResult invalidMsg = ValidUtils.valid(toValidPar, validGroup);
            if (invalidMsg != null) {
                microResp.setResultCode(RespCodeUtils.code("", thisMethodMeta.getModelCode(),
                        RespCodeUtils.RESULT_CODE__INVALID_INPUT));
                microResp.setMessage(invalidMsg);
                return false;
            }
        }
        return true;
    }

    /**
     * 请求的批量数据，在不同的事务中进行处理
     * @param point 切点
     * @param validGroup 数据校验组
     * @param thisMethodMeta 切点方法的元数据
     * @param microReq 请求体
     * @param method 切点方法
     * @param backAllReqData 备份的原始请求数据
     * @return 生成的响应体
     */
    private MicroResp<?> mulTransProcess(ProceedingJoinPoint point, Class validGroup, RestMeta thisMethodMeta,
                                         MicroReq<?> microReq, Method method, List<BatchReq<?>> backAllReqData) {

        // 1.准备初始数据
        MicroResp<?> microResp = new MicroResp();  // 准备应答内容
        List<BatchResp> everyRespList = Lists.newArrayList();
        microResp.setData((List) everyRespList);

        String succString = thisMethodMeta.getSuccStr(); // 获取返回的成功提示语
        String faildString = thisMethodMeta.getFaildStr(); // 获取返回的失败提示语
        String modelCode = thisMethodMeta.getModelCode(); // 获取返回的模块代码

        // 2.从备份的原始消息数据中，遍历获取每条数据进行处理
        int index = 1;
        for (BatchReq<?> batchReqObj : backAllReqData) {

            // 2.1 生成每个数据的应答数据结构
            BatchResp resp = new BatchResp<Object>(
                    batchReqObj.getItemId() == null ? String.valueOf(index++) : batchReqObj.getItemId());
            everyRespList.add(resp);

            try {
                Object dataObj = batchReqObj.getData();

                // 2.2 校验body中要处理数据集合
                ValidResult invalidMsg = ValidUtils.valid(dataObj, validGroup);
                if (invalidMsg != null) {
                    resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INVALID_INPUT));
                    resp.setMessage(invalidMsg);
                    continue;
                }

                // 2.3 将要处理的数据，装入切点的参数中，并调用切点中的逻辑处理
                microReq.getData().add((BatchReq) batchReqObj);
                Object result = point.proceed();
                if (StringUtils.isBlank(microResp.getResultCode())) {
                    resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__SUCC));
                    if (StringUtils.isBlank(microResp.getMessage().getDescription())) {
                        resp.getMessage().setDescription(succString);
                    }
                }

                // 2.4 将应答数据装箱
                resp.setData(((MicroResp<?>) result).getData().get(0).getData());
            } catch (Throwable e) {
                CRUDRest annotation = method.getAnnotation(CRUDRest.class);
                Class exception = annotation.exception();
                processCUDException(faildString, modelCode, resp, e, exception);
            } finally {
                // 无论是否有异常情况，都需要将入参信息清空。
                microReq.getData().clear();
            }
        }
        return microResp;
    }

    /**
     * 请求的批量数据，在不同的事务中进行处理
     * @param point 切点
     * @param validGroup 数据校验组
     * @param thisMethodMeta 切点方法的元数据
     * @param microReq 请求体
     * @param method 切点方法
     * @param backAllReqData 备份的原始请求数据
     * @return 生成的响应体
     */
    private MicroResp<?> singleTransProcess(ProceedingJoinPoint point, Class validGroup, RestMeta thisMethodMeta,
                                            MicroReq<?> microReq, Method method, List<BatchReq<?>> backAllReqData) {

        // 1.准备初始数据
        MicroResp<?> microResp = new MicroResp();  // 准备应答内容
        List<BatchResp> everyRespList = Lists.newArrayList();
        microResp.setData((List) everyRespList);

        String succString = thisMethodMeta.getSuccStr(); // 获取返回的成功提示语
        String faildString = thisMethodMeta.getFaildStr(); // 获取返回的失败提示语
        String modelCode = thisMethodMeta.getModelCode(); // 获取返回的模块代码

        // 2.从备份的原始消息数据中，遍历获取每条数据进行校验
        int index = 1;
        for (BatchReq batchReqObj : backAllReqData) {
            BatchResp validErrorResp = new BatchResp<Object>(
                    batchReqObj.getItemId() == null ? String.valueOf(index++) : batchReqObj.getItemId());
            try {
                Object dataObj = batchReqObj.getData();

                // 校验body中要处理数据集合
                ValidResult invalidMsg = ValidUtils.valid(dataObj, validGroup);
                if (invalidMsg != null) {
                    validErrorResp
                            .setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INVALID_INPUT));
                    validErrorResp.setMessage(invalidMsg);
                    everyRespList.add(validErrorResp);
                }

            } catch (Throwable e) {
                CRUDRest annotation = method.getAnnotation(CRUDRest.class);
                Class exception = annotation.exception();
                processCUDException(faildString, modelCode, validErrorResp, e, exception);
            }
        }
        if (CollectionUtils.isNotEmpty(everyRespList)) {// 如果验证未通过，则返回错误信息
            microResp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INVALID_INPUT));
            microResp.getMessage().setDescription("数据校验失败");
            microResp.setData((List) everyRespList);
            return microResp;
        }

        // 3.开始进行业务处理
        try {
            microReq.getData().addAll((List) backAllReqData);
            microResp = (MicroResp<?>) point.proceed();
            if (StringUtils.isBlank(microResp.getResultCode())) {
                microResp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__SUCC));
                if (StringUtils.isBlank(microResp.getMessage().getDescription())) {
                    microResp.getMessage().setDescription(succString);
                }
            }
        } catch (Throwable e) {
            CRUDRest annotation = method.getAnnotation(CRUDRest.class);
            Class exception = annotation.exception();
            processCUDException(faildString, modelCode, microResp, e, exception);
        }

        return microResp;
    }

    /**
     * 处理查询异常，并做为失败结果装入response中
     * @param point AOP被切方法
     * @param faildString 失败提示语
     * @param modelCode 业务模块编码
     * @param e 异常
     * @param businessException 业务异常.如果是业务异常,Rest应答消息不会显示内部异常
     */
    private BaseResp processQryException(ProceedingJoinPoint point, String faildString, String modelCode, Throwable e, Class businessException) throws IllegalAccessException, InstantiationException, CRUDException {
        Method method = fetchPointMethod(point);
        BaseResp queryResp = this.genDefaultResp(method);
        if (businessException.isAssignableFrom(e.getClass())) {
            queryResp.setResultCode(modelCode + ((BaseException) e).getErrorCode());
            queryResp.getMessage().setDescription(e.getMessage());
        } else {
            log.error(faildString, e);
            queryResp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INNER_ERROR));
            queryResp.getMessage().setDescription(faildString);
        }
        return queryResp;
    }

    /**
     * 处理CUD异常，并做为失败结果装入response中
     * @param faildString 失败提示语
     * @param modelCode 业务模块编码
     * @param resp 应答消息。异常消息将会封装在此response中
     * @param e 异常
     * @param businessException 业务异常.如果是业务异常,Rest应答消息不会显示内部异常
     */
    private void processCUDException(String faildString, String modelCode, BaseResp resp, Throwable e,
                                     Class businessException) {
        if (businessException.isAssignableFrom(e.getClass())) {
            resp.setResultCode(modelCode + ((BaseException) e).getErrorCode());
            resp.getMessage().setDescription(e.getMessage());
        } else {
            log.error(faildString, e);
            resp.setResultCode(RespCodeUtils.code("", modelCode, RespCodeUtils.RESULT_CODE__INNER_ERROR));
            resp.getMessage().setDescription(faildString);
        }
    }

    public static class RestMeta {

        /**
         * 操作类型
         */
        private CRUDRest.Type type;

        /**
         * 成功提示语
         */
        private String succStr;

        /**
         * 失败提示语
         */
        private String faildStr;

        /**
         * 模块编码
         */
        private String modelCode;

        /**
         * 本模块抛出的自定义异常
         */
        private Class exception;

        public CRUDRest.Type getType() {
            return type;
        }

        public void setType(CRUDRest.Type type) {
            this.type = type;
        }

        public String getSuccStr() {
            return succStr;
        }

        public void setSuccStr(String succStr) {
            this.succStr = succStr;
        }

        public String getFaildStr() {
            return faildStr;
        }

        public void setFaildStr(String faildStr) {
            this.faildStr = faildStr;
        }

        public String getModelCode() {
            return modelCode;
        }

        public void setModelCode(String modelCode) {
            this.modelCode = modelCode;
        }

        public Class getException() {
            return exception;
        }

        public void setException(Class exception) {
            this.exception = exception;
        }

        /**
         * 生成元数据
         * @param cRUDRestAnnotation 类或方法上设置的枚举
         * @return 生成好的元数据
         */
        public static RestMeta genMeta(CRUDRest cRUDRestAnnotation) {
            RestMeta meta = new RestMeta();
            meta.setException(cRUDRestAnnotation.exception());
            meta.setFaildStr(cRUDRestAnnotation.faildStr());
            meta.setModelCode(cRUDRestAnnotation.modelCode());
            meta.setSuccStr(cRUDRestAnnotation.succStr());
            meta.setType(cRUDRestAnnotation.type());
            return meta;
        }
    }

}
