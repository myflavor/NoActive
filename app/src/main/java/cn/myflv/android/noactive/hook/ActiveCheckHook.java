package cn.myflv.android.noactive.hook;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.Parcel;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;

public class ActiveCheckHook extends XC_MethodHook {

    public static final int TRANSACTION = 'N' << 24 | 'A' << 16 | 'X' << 8 | 'P';
    private static final String DESCRIPTOR = "android.content.IClipboard";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) {
        int code = (int) param.args[0];
        long dataObj = (long) param.args[1];
        long replyObj = (long) param.args[2];
        int flags = (int) param.args[3];
        if (code == TRANSACTION) {
            param.setResult(execTransact(code, dataObj, replyObj, flags));
        }
    }

    private boolean execTransact(int code, long dataObj, long replyObj, int flags) {
        Parcel data = parcelFromNativePointer(dataObj);
        if (data == null) {
            return false;
        }
        Parcel reply = parcelFromNativePointer(replyObj);
        boolean res = false;
        try {
            res = onTransact(code, data, reply, flags);
        } catch (Exception e) {
            if (reply != null) {
                reply.setDataPosition(0);
                reply.writeException(e);
            }
        } finally {
            data.setDataPosition(0);
            if (reply != null) {
                reply.setDataPosition(0);
            }
        }
        if (res) {
            data.recycle();
            if (reply != null) {
                reply.recycle();
            }
        }
        return res;
    }

    private boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
        data.enforceInterface(DESCRIPTOR);
        if (data.readInt() == 1) {
            if (reply == null) {
                return false;
            }
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        }
        return false;
    }

    private Parcel parcelFromNativePointer(long ptr) {
        try {
            @SuppressLint("SoonBlockedPrivateApi") Method method = Parcel.class.getDeclaredMethod("obtain", long.class);
            method.setAccessible(true);
            return (Parcel) method.invoke(null, ptr);
        } catch (Exception e) {
            return null;
        }
    }

    // for app use
    public static boolean checkServiceIsActive() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            @SuppressLint("PrivateApi") Class<?> ServiceManagerClass = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = ServiceManagerClass.getMethod("getService", String.class);
            getServiceMethod.setAccessible(true);
            IBinder delegateService = (IBinder) getServiceMethod.invoke(null, "clipboard");
            data.writeInterfaceToken(DESCRIPTOR);
            data.writeInt(1);
            boolean status = delegateService.transact(TRANSACTION, data, reply, 0);
            reply.readException();
            if (status) {
                return reply.readInt() == 1;
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

}
