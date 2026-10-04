package ltlf.external.spot;

import ltlf.ast.Formula;

public final class SpotNativeBridge{

    static{
        System.loadLibrary("ltlf_spot_bridge");
    }

    private SpotNativeBridge(){
    }

    public static native boolean equivalent(String left, String right);

    public static native Formula parse(String input) ;
}