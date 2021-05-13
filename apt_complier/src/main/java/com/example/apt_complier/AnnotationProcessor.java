package com.example.apt_complier;

import com.example.apt_annotations.SubscribMethod;
import com.example.apt_annotations.Subscribe;
import com.example.apt_annotations.ThreadMode;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by bohou on 2021/4/6 Email:bohou@tencent.com
 */

/**
 * Automatically generate META-INF file-used to register custom annotation processor
 * Specify which annotation the processor handles
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.example.apt_annotations.Subscribe")
public class AnnotationProcessor extends AbstractProcessor {

    private static final String GETSUBSCRIBEMETHOD = "getSubscribeMethod";
    private static final String APTBOHOUEVENTBUSPOLICY = "APTBohouEventBusPolicy";
    private static final String ANNOTATIONPROCESSOR_PACKAGE_NAME = "com.example.aptapplication";
    private static final int FIRST_PARAM = 0;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        /**
         * Generic type：<fullClassName,CreateMethod>
         */
        Map<String, CreateMethod> mCacheCreatMethod = new HashMap<>();
        /**
         * private static Map<Object, List<SubscribMethod>> cacheMap = new HashMap<>();
         */
        ParameterizedTypeName listParameterizedTypeName = ParameterizedTypeName.get(List.class, SubscribMethod.class);
        ClassName mapClassName = ClassName.get(HashMap.class);
        ClassName objClassName = ClassName.get(Object.class);
        FieldSpec cacheMapFieldSpec = FieldSpec
                .builder(ParameterizedTypeName.get(mapClassName, objClassName, listParameterizedTypeName), "cacheMap")
                .addModifiers(Modifier.STATIC, Modifier.PRIVATE)
                .initializer("new $T<>()", HashMap.class)
                .build();
        /**
         * public static List<SubscribMethod> getSubscribeMethod(Object obj) {
         *     return cacheMap.get(obj);
         * }
         */
        MethodSpec getSubscribeMethodSpec = MethodSpec.methodBuilder(GETSUBSCRIBEMETHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, SubscribMethod.class))
                .addParameter(Object.class, "registedClass")
                .addStatement("return cacheMap.get(registedClass)")
                .build();

        /**
         * Class's Builder
         */
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(APTBOHOUEVENTBUSPOLICY)
                .addModifiers(Modifier.PUBLIC)
                .addField(cacheMapFieldSpec)
                .addMethod(getSubscribeMethodSpec);

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Subscribe.class);
        if (elements == null || elements.size() == 0) {
            return false;
        }
        Iterator<? extends Element> iterator = elements.iterator();
        while (iterator.hasNext()) {
            ExecutableElement executableElement = (ExecutableElement) iterator.next();
            /**
             * Class element
             */
            TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
            /**
             * parameter list
             */
            List<? extends VariableElement> methodParameters = executableElement.getParameters();
            if (methodParameters == null) {
                return false;
            }
            TypeMirror methodParamsType = methodParameters.get(FIRST_PARAM).asType();
            /**
             * The value of the annotation
             */
            Subscribe annotation = executableElement.getAnnotation(Subscribe.class);
            ThreadMode threadMode = annotation.threadMode();
            /**
             * Simple method name
             */
            String simpleMethodName = executableElement.getSimpleName().toString();
            /**
             * Full class name
             */
            String fullClassName = typeElement.getQualifiedName().toString();
            /**
             * The key of mCacheCreatMethod --> class_name + method_name
             */
            String keyOfMap = simpleMethodName + fullClassName;
            CreateMethod methodFromCache = mCacheCreatMethod.get(keyOfMap);
            if (methodFromCache == null) {
                CreateMethod createMethod = new CreateMethod(fullClassName, simpleMethodName, methodParamsType, threadMode);
                mCacheCreatMethod.put(keyOfMap, createMethod);
            }
        }
        /**
         * After the while loop is over, all annotation-modified elements will be saved to mCacheCreatMethod
         * Static code block
         */
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        /**
         * Traverse mCacheCreatMethod to create methods corresponding to different classes
         */
        for (String key : mCacheCreatMethod.keySet()) {
            CreateMethod createMethod = mCacheCreatMethod.get(key);
            /**
             * public static void setCacheMap() {
             *         cacheMap.put(MainActivity.class, getMainSubscribeMethod());
             *         cacheMap.put(SecondActivity.class,getSecondSubscribeMethod());
             *     }
             */
            typeSpecBuilder.addMethod(createMethod.generateMethod());
            codeBlockBuilder.addStatement("cacheMap.put($L.class,$L())", createMethod.getFullClassName(), createMethod.getMethodName());
        }

        /**
         * Class add static block
         */
        TypeSpec typeSpec = typeSpecBuilder.addStaticBlock(codeBlockBuilder.build()).build();
        /**
         * Put the class in the specified file
         */
        JavaFile javaFile = JavaFile.builder(ANNOTATIONPROCESSOR_PACKAGE_NAME, typeSpec)
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
