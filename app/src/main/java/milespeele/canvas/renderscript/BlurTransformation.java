//package milespeele.canvas.renderscript;
//
//import android.app.Application;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.Build;
//import android.renderscript.Allocation;
//import android.renderscript.Element;
//import android.renderscript.RenderScript;
//import android.renderscript.ScriptIntrinsicBlur;
//
//import com.squareup.picasso.Transformation;
//
//import javax.inject.Inject;
//
//import milespeele.canvas.MainApp;
//
//public class BlurTransformation implements Transformation {
//
//    @Override
//    public Bitmap transform(Bitmap inBitmap) {
//        if (Build.CPU_ABI.contains("armeabi-v7a")) {
//            Bitmap outBitmap = inBitmap.copy(inBitmap.getConfig(), true);
//
//            // Init the RenderScript
//            final RenderScript rs = RenderScript.create();
//
//            // Allocate memory
//            // NOTE: This must be done from the Java side, not the C side
//            Allocation inAlloc = Allocation.createFromBitmap(rs, inBitmap,
//                    Allocation.MipmapControl.MIPMAP_NONE,
//                    Allocation.USAGE_SHARED | Allocation.USAGE_GRAPHICS_TEXTURE | Allocation.USAGE_SCRIPT);
//            Allocation outAlloc = Allocation.createTyped(rs, inAlloc.getType());
//
//            // Init the blur script
//            final ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//            blurScript.setRadius(25.0f);
//            blurScript.setInput(inAlloc);
//            blurScript.forEach(outAlloc);
//
//            // Transfer the allocation back to a bitmap
//            outAlloc.copyTo(outBitmap);
//
//            // Cleanup extra supplies
//            inBitmap.recycle();
//            rs.destroy();
//            return outBitmap;
//
//        }
//        return inBitmap;
//    }
//
//    @Override
//    public String key() {
//        return "blur";
//    }
//}