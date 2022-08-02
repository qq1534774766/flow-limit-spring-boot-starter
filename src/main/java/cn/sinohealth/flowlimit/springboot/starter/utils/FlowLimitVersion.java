package cn.sinohealth.flowlimit.springboot.starter.utils;

public class FlowLimitVersion {

    private FlowLimitVersion() {
    }

    public static String getVersion() {
        Package pkg = FlowLimitVersion.class.getPackage();
        return (pkg != null ? pkg.getImplementationVersion() : null);
    }

}