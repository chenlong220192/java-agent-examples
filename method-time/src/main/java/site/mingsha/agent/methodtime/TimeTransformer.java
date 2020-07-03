package site.mingsha.agent.methodtime;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author mingsha
 * @date 2020-06-15
 */
public class TimeTransformer implements ClassFileTransformer {

    /**
     *
     * @param loader
     * @param className
     * @param classBeingRedefined
     * @param protectionDomain
     * @param classfileBuffer
     * @return
     */
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        if (!className.startsWith("site/mingsha/agent/demo/")) {
            return classfileBuffer;
        }

        CtClass cl;
        try {
            ClassPool classPool = ClassPool.getDefault();
            cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

            for (CtMethod method : cl.getDeclaredMethods()) {
                // 统计所有方法调用耗时
                method.addLocalVariable("start", CtClass.longType);
                method.insertBefore("start = System.currentTimeMillis();");
                String methodName = method.getLongName();
                method.insertAfter(
                        "System.out.println(\"" + methodName + " cost: \" + (System" + ".currentTimeMillis() - start));",
                        // 该参数设置为true
                        // 表示即便抛出异常了，下面的代码也会执行；
                        true
                );
            }

            byte[] transformed = cl.toBytecode();
            return transformed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }

}
