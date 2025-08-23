package io.github.cctyl.utils;

import java.math.BigInteger;

/**
 * @author cctyl
 */
public class AVBVConverter {

    private static final BigInteger XOR_CODE = BigInteger.valueOf(23442827791579L);
    private static final BigInteger MASK_CODE = BigInteger.valueOf(2251799813685247L);
    private static final BigInteger MAX_AID = BigInteger.ONE.shiftLeft(51);
    private static final int BASE = 58;

    private static final String DATA = "FcwAPNKTMug3GV5Lj7EJnHpWsx4tb8haYeviqBz6rkCy12mUSDQX9RdoZf";

    public static String av2bv(long aidParam) {

        BigInteger aid = BigInteger.valueOf(aidParam);
        char[] bytes = {'B', 'V', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
        int bvIndex = bytes.length - 1;
        BigInteger tmp = MAX_AID.or(aid).xor(XOR_CODE);
        while (tmp.compareTo(BigInteger.ZERO) > 0) {
            bytes[bvIndex] = DATA.charAt(tmp.mod(BigInteger.valueOf(BASE)).intValue());
            tmp = tmp.divide(BigInteger.valueOf(BASE));
            bvIndex -= 1;
        }
        swap(bytes, 3, 9);
        swap(bytes, 4, 7);
        StringBuilder sb = new StringBuilder(bytes.length);
        for (Character ch : bytes) {
            sb.append(ch);
        }
        return sb.toString();
    }

    public static Long bv2av(String bvid) {
        char[] bvidArr = bvid.toCharArray();
        swap(bvidArr, 3, 9);
        swap(bvidArr, 4, 7);
        String adjustedBvid = new String(bvidArr, 3, bvidArr.length - 3);
        BigInteger tmp = BigInteger.ZERO;
        for (char c : adjustedBvid.toCharArray()) {
            tmp = tmp.multiply(BigInteger.valueOf(BASE)).add(BigInteger.valueOf(DATA.indexOf(c)));
        }
        BigInteger xor = tmp.and(MASK_CODE).xor(XOR_CODE);
        return xor.longValue();
    }


    private static void swap(char[] array, int i, int j) {
        char temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static void main(String[] args) {

        final int aid1 = 643755790;
        final String bv1 = "BV1bY4y1j7RA";

        final int aid2 = 305988942;
        final String bv2 = "BV1aP411K7it";


        //av ==> bv
        assert av2bv(aid1).equals(bv1);
        assert av2bv(aid2).equals(bv2);


        //bv ==>av
        assert bv2av(bv1) == aid1;
        assert bv2av(bv2) == aid2;
    }
}