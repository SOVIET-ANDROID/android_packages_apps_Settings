package com.android.settings;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.format.DateUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.InetAddress;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class UtilsTest {

    private Context mContext;
    @Mock private WifiManager wifiManager;
    @Mock private Network network;
    @Mock private ConnectivityManager connectivityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mContext = spy(RuntimeEnvironment.application);
        when(mContext.getSystemService(WifiManager.class)).thenReturn(wifiManager);
        when(mContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(connectivityManager);
    }

    @Test
    public void testGetWifiIpAddresses_succeeds() throws Exception {
        when(wifiManager.getCurrentNetwork()).thenReturn(network);
        LinkAddress address = new LinkAddress(InetAddress.getByName("127.0.0.1"), 0);
        LinkProperties lp = new LinkProperties();
        lp.addLinkAddress(address);
        when(connectivityManager.getLinkProperties(network)).thenReturn(lp);

        assertThat(Utils.getWifiIpAddresses(mContext)).isEqualTo("127.0.0.1");
    }

    @Test
    public void testGetWifiIpAddresses_nullLinkProperties() {
        when(wifiManager.getCurrentNetwork()).thenReturn(network);
        // Explicitly set the return value to null for readability sake.
        when(connectivityManager.getLinkProperties(network)).thenReturn(null);

        assertThat(Utils.getWifiIpAddresses(mContext)).isNull();
    }

    @Test
    public void testGetWifiIpAddresses_nullNetwork() {
        // Explicitly set the return value to null for readability sake.
        when(wifiManager.getCurrentNetwork()).thenReturn(null);

        assertThat(Utils.getWifiIpAddresses(mContext)).isNull();
    }

    @Test
    public void testAssignDefaultPhoto_ContextNull_ReturnFalseAndNotCrash() {
        // Should not crash here
        assertThat(Utils.assignDefaultPhoto(null, 0)).isFalse();
    }

    @Test
    public void testFormatElapsedTime_WithSeconds_ShowSeconds() {
        final double testMillis = 5 * DateUtils.MINUTE_IN_MILLIS;
        final String expectedTime = "5m 0s";

        assertThat(Utils.formatElapsedTime(mContext, testMillis, true)).isEqualTo(expectedTime);
    }

    @Test
    public void testFormatElapsedTime_NoSeconds_DoNotShowSeconds() {
        final double testMillis = 5 * DateUtils.MINUTE_IN_MILLIS;
        final String expectedTime = "5m";

        assertThat(Utils.formatElapsedTime(mContext, testMillis, false)).isEqualTo(expectedTime);
    }

    @Test
    public void testFormatElapsedTime_TimeMoreThanOneDay_ShowCorrectly() {
        final double testMillis = 2 * DateUtils.DAY_IN_MILLIS
                + 4 * DateUtils.HOUR_IN_MILLIS + 15 * DateUtils.MINUTE_IN_MILLIS;
        final String expectedTime = "2d 4h 15m";

        assertThat(Utils.formatElapsedTime(mContext, testMillis, false)).isEqualTo(expectedTime);
    }

    @Test
    public void testInitializeVolumeDoesntBreakOnNullVolume() {
        VolumeInfo info = new VolumeInfo("id", 0, new DiskInfo("id", 0), "");
        StorageManager storageManager = mock(StorageManager.class, RETURNS_DEEP_STUBS);
        when(storageManager.findVolumeById(anyString())).thenReturn(info);

        Utils.maybeInitializeVolume(storageManager, new Bundle());
    }
}
