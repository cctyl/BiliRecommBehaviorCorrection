package io.github.cctyl.interceptor;

import bilibili.metadata.MetadataRpcProto;
import bilibili.metadata.device.DeviceRpcProto;
import bilibili.metadata.fawkes.FawkesRpcProto;
import bilibili.metadata.locale.LocaleRpcProto;
import bilibili.metadata.network.NetworkRpcProto;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.config.GlobalVariables;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.grpc.Metadata.BINARY_BYTE_MARSHALLER;

/**
 * grpc拦截器
 */
@Slf4j
@GrpcGlobalClientInterceptor
public class GrpcInterceptor implements ClientInterceptor {

    @Autowired
    private BiliApi biliApi;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> methodDescriptor,
            CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall
                .SimpleForwardingClientCall<>
                (channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                log.debug("经过grpc拦截器");
                headers.put(
                        Metadata.Key.of("authorization", ASCII_STRING_MARSHALLER),
                        "identify_v1 "  + biliApi.getAccessKey(false)
                );

                headers.put(
                        Metadata.Key.of("user-agent", ASCII_STRING_MARSHALLER),
                        "Dalvik/2.1.0 (Linux; U; Android 12; NOH-AN01 Build/HUAWEINOH-AN01) 7.38.0 os/android model/NOH-AN01 mobi_app/android build/7380300 channel/master innerVer/7380310 osVer/12 network/2 grpc-java-cronet/1.36.1"
                );

                headers.put(
                        Metadata.Key.of("x-bili-aurora-eid", ASCII_STRING_MARSHALLER),
                        genAuroraEid(GlobalVariables.getMID())
                );
                headers.put(
                        Metadata.Key.of("x-bili-mid", ASCII_STRING_MARSHALLER),
                        GlobalVariables.getMID()
                );
                headers.put(
                        Metadata.Key.of("x-bili-aurora-zone", ASCII_STRING_MARSHALLER),
                        ""
                );
                headers.put(
                        Metadata.Key.of("x-bili-trace-id", ASCII_STRING_MARSHALLER),
                        genTraceId()
                );
                headers.put(
                        Metadata.Key.of("buvid", ASCII_STRING_MARSHALLER),
                        fakeBuvid()
                );
                headers.put(
                        Metadata.Key.of("bili-http-engine", ASCII_STRING_MARSHALLER),
                        "cronet"
                );

                headers.put(
                        Metadata.Key.of("x-bili-fawkes-req-bin", BINARY_BYTE_MARSHALLER),
                        generateFawkesReqBin()
                );

                headers.put(
                        Metadata.Key.of("x-bili-metadata-bin", BINARY_BYTE_MARSHALLER),
                        generateMetadataBin()
                );
                headers.put(
                        Metadata.Key.of("x-bili-device-bin", BINARY_BYTE_MARSHALLER),
                        generateDeviceBin()
                );
                headers.put(
                        Metadata.Key.of("x-bili-network-bin", BINARY_BYTE_MARSHALLER),
                        generateNetworkBin()
                );
                headers.put(
                        Metadata.Key.of("x-bili-restriction-bin", BINARY_BYTE_MARSHALLER),
                        new byte[0]
                );
                headers.put(
                        Metadata.Key.of("x-bili-locale-bin", BINARY_BYTE_MARSHALLER),
                        generateLocaleBin()
                );
                headers.put(
                        Metadata.Key.of("x-bili-exps-bin", BINARY_BYTE_MARSHALLER),
                        new byte[0]
                );

                super.start(responseListener, headers);
            }
        };
    }

    private byte[] generateLocaleBin() {
        return LocaleRpcProto.Locale.newBuilder()
                .setCLocale(LocaleRpcProto.LocaleIds.newBuilder()
                        .setLanguage("zh")
                        .setRegion("CN")
                        .build())
                .build()
                .toByteArray();
    }

    private byte[] generateNetworkBin() {

        return NetworkRpcProto.Network.newBuilder()
                .setType(NetworkRpcProto.NetworkType.CELLULAR)
                .setTf(NetworkRpcProto.TFType.T_CARD)
                .build()
                .toByteArray();
    }

    private byte[] generateDeviceBin() {

        return DeviceRpcProto.Device.newBuilder()
                .setMobiApp("android")
                .setPlatform("android")
                .setBuild(7380300)
                .setChannel("alifenfa")
                .setBuvid(fakeBuvid())
                .setDevice("phone")
                .setOsver("10")
                .setModel("HUAWEI")
                .setVersionName("7.38.0")
                .build()
                .toByteArray();

    }

    private byte[] generateMetadataBin() {

        return MetadataRpcProto.Metadata.newBuilder()
                .setAccessKey(biliApi.getAccessKey(false))
                .setMobiApp("android")
                .setPlatform("android")
                .setBuild(7380300)
                .setChannel("alifenfa")
                .setBuvid(fakeBuvid())
                .setDevice("phone")
                .build()
                .toByteArray();

    }


    public static String randomId() {
        String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public static String fakeBuvid() {
        StringBuilder macBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            StringBuilder randStr = new StringBuilder();
            for (int j = 0; j < 2; j++) {
                int randIndex = random.nextInt(16);
                randStr.append(Integer.toHexString(randIndex));
            }
            macBuilder.append(randStr);
            if (i != 5) {
                macBuilder.append(":");
            }
        }
        String randMac = macBuilder.toString();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5MacBytes = md5.digest(randMac.getBytes());
            StringBuilder md5MacBuilder = new StringBuilder();
            for (byte b : md5MacBytes) {
                md5MacBuilder.append(String.format("%02x", b & 0xff));
            }
            String md5MacStr = md5MacBuilder.toString();
            char[] md5MacChars = md5MacStr.toCharArray();
            return "XY" + md5MacChars[2] + md5MacChars[12] + md5MacChars[22] + md5MacStr;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        }
        return "";
    }

    public static String genAuroraEid(String uid) {
        byte[] midBytes = uid.getBytes();
        byte[] resultBytes = new byte[midBytes.length];
        byte[] key = "ad1va46a7lza".getBytes();
        for (int i = 0; i < midBytes.length; i++) {
            resultBytes[i] = (byte) (midBytes[i] ^ key[i % key.length]);
        }
        String result = Base64.getEncoder().encodeToString(resultBytes);
        return result.replace("=", "");
    }

    public String genTraceId() {
        StringBuilder randomId = new StringBuilder();
        String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        for (int i = 0; i < 32; i++) {
            int index = random.nextInt(characters.length());
            randomId.append(characters.charAt(index));
        }
        String randomTraceId = randomId.substring(0, 24);
        byte[] bArr = new byte[3];
        int ts = (int) (System.currentTimeMillis() / 1000);
        for (int i = 2; i >= 0; i--) {
            ts >>= 8;
            if ((ts / 128) % 2 == 0) {
                bArr[i] = (byte) (ts % 256);
            } else {
                bArr[i] = (byte) (ts % 256 - 256);
            }
        }
        StringBuilder traceIdBuilder = new StringBuilder(randomTraceId);
        for (byte b : bArr) {
            traceIdBuilder.append(String.format("%02x", b & 0xFF));
        }
        traceIdBuilder.append(randomId.substring(randomId.length() - 2));
        return traceIdBuilder + ":" + traceIdBuilder.substring(16) + ":0:0";
    }


    private static byte[] generateFawkesReqBin() {
        return FawkesRpcProto.FawkesReq.newBuilder()
                .setAppkey("android64")
                .setEnv("prod")
                .setSessionId(randomId())
                .build()
                .toByteArray();
    }
}
