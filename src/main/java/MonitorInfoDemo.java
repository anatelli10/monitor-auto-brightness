import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Dxva2;
import com.sun.jna.platform.win32.PhysicalMonitorEnumerationAPI.PHYSICAL_MONITOR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WTypes.LPSTR;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORENUMPROC;
import com.sun.jna.platform.win32.WinUser.MONITORINFOEX;

public class MonitorInfoDemo {
    public static void setBrightness(final int brightness)
    {
//        System.out.println("Installed Physical Monitors: " + User32.INSTANCE.GetSystemMetrics(WinUser.SM_CMONITORS));

        User32.INSTANCE.EnumDisplayMonitors(null, null, new MONITORENUMPROC() {

            @Override
            public int apply(HMONITOR hMonitor, HDC hdc, RECT rect, LPARAM lparam)
            {
                enumerate(hMonitor, brightness);

                return 1;
            }

        }, new LPARAM(0));
    }

    static void enumerate(HMONITOR hMonitor, final int brightness)
    {
        MONITORINFOEX info = new MONITORINFOEX();
        User32.INSTANCE.GetMonitorInfo(hMonitor, info);

        DWORDByReference pdwNumberOfPhysicalMonitors = new DWORDByReference();
        Dxva2.INSTANCE.GetNumberOfPhysicalMonitorsFromHMONITOR(hMonitor, pdwNumberOfPhysicalMonitors);
        int monitorCount = pdwNumberOfPhysicalMonitors.getValue().intValue();

        PHYSICAL_MONITOR[] physMons = new PHYSICAL_MONITOR[monitorCount];
        Dxva2.INSTANCE.GetPhysicalMonitorsFromHMONITOR(hMonitor, monitorCount, physMons);

        for (int i = 0; i < monitorCount; i++)
        {
            HANDLE hPhysicalMonitor = physMons[0].hPhysicalMonitor;
            enumeratePhysicalMonitor(i, hPhysicalMonitor, brightness);
        }

        Dxva2.INSTANCE.DestroyPhysicalMonitors(monitorCount, physMons);
    }

    /**
     * @param hPhysicalMonitor
     */
    private static void enumeratePhysicalMonitor(int index, HANDLE hPhysicalMonitor, final int brightness)
    {
        // Brightness
        DWORDByReference pdwMinimumBrightness = new DWORDByReference();
        DWORDByReference pdwCurrentBrightness = new DWORDByReference();
        DWORDByReference pdwMaximumBrightness = new DWORDByReference();
        Dxva2.INSTANCE.GetMonitorBrightness(hPhysicalMonitor, pdwMinimumBrightness, pdwCurrentBrightness, pdwMaximumBrightness);

        // Capabilities string
        DWORDByReference pdwCapabilitiesStringLengthInCharacters = new DWORDByReference();
        Dxva2.INSTANCE.GetCapabilitiesStringLength(hPhysicalMonitor, pdwCapabilitiesStringLengthInCharacters);
        DWORD capStrLen = pdwCapabilitiesStringLengthInCharacters.getValue();

        LPSTR pszASCIICapabilitiesString = new LPSTR(new Memory(capStrLen.intValue()));
        Dxva2.INSTANCE.CapabilitiesRequestAndCapabilitiesReply(hPhysicalMonitor, pszASCIICapabilitiesString, capStrLen);
        String test = new String(pszASCIICapabilitiesString.getPointer().getString(0));
        String matchString = "model(";
        test = test.substring(test.indexOf(matchString) + matchString.length());
        test = test.substring(0, test.indexOf((")")));

        System.out.println("Changing " + test + " brightness from " + pdwCurrentBrightness.getValue() + " to " + brightness);
        Dxva2.INSTANCE.SetMonitorBrightness(hPhysicalMonitor, brightness);

    }
}
