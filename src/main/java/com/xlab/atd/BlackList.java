package com.xlab.atd;

import java.util.Arrays;

public class BlackList {
    private static long[] denyHashCodes = new long[]{
            // from 1.2.67
            0x80D0C70BCC2FEA02L,
            0x86FC2BF9BEAF7AEFL,
            0x87F52A1B07EA33A6L,
            0x8EADD40CB2A94443L,
            0x8F75F9FA0DF03F80L,
            0x9172A53F157930AFL,
            0x92122D710E364FB8L,
            0x941866E73BEFF4C9L,
            0x94305C26580F73C5L,
            0x9437792831DF7D3FL,
            0xA123A62F93178B20L,
            0xA85882CE1044C450L,
            0xAA3DAFFDB10C4937L,
            0xAC6262F52C98AA39L,
            0xAD937A449831E8A0L,
            0xAE50DA1FAD60A096L,
            0xAFFF4C95B99A334DL,
            0xB40F341C746EC94FL,
            0xB7E8ED757F5D13A2L,
            0xBCDD9DC12766F0CEL,
            0xC00BE1DEBAF2808BL,
            0xC2664D0958ECFE4CL,
            0xC7599EBFE3E72406L,
            0xC8D49E5601E661A9L,
            0xC963695082FD728EL,
            0xD1EFCDF4B3316D34L,
            0xDE23A0809A8B9BD6L,
            0xDEFC208F237D4104L,
            0xDF2DDFF310CDB375L,
            0xE09AE4604842582FL,
            0xE1919804D5BF468FL,
            0xE2EB3AC7E56C467EL,
            0xE603D6A51FAD692BL,
            0xE9184BE55B1D962AL,
            0xE9F20BAD25F60807L,
            0xF3702A4A5490B8E8L,
            0xF474E44518F26736L,
            0xF7E96E74DFA58DBCL,
            0xFC773AE20C827691L,
            0xFD5BFC610056D720L,
            0xFFA15BF021F1E37CL,
            0xFFDD1A80F1ED3405L,
            0x10E067CD55C5E5L,
            0x761619136CC13EL,
            0x3085068CB7201B8L,
            0x45B11BC78A3ABA3L,
            0x55CFCA0F2281C07L,
            0xB6E292FA5955ADEL,
            0xEE6511B66FD5EF0L,
            0x100150A253996624L,
            0x10B2BDCA849D9B3EL,
            0x144277B467723158L,
            0x14DB2E6FEAD04AF0L,
            0x154B6CB22D294CFAL,
            0x17924CCA5227622AL,
            0x193B2697EAAED41AL,
            0x1E0A8C3358FF3DAEL,
            0x24D2F6048FEF4E49L,
            0x24EC99D5E7DC5571L,
            0x25E962F1C28F71A2L,
            0x275D0732B877AF29L,
            0x2ADFEFBBFE29D931L,
            0x2B3A37467A344CDFL,
            0x2D308DBBC851B0D8L,
            0x313BB4ABD8D4554CL,
            0x327C8ED7C8706905L,
            0x332F0B5369A18310L,
            0x339A3E0B6BEEBEE9L,
            0x33C64B921F523F2FL,
            0x34A81EE78429FDF1L,
            0x3826F4B2380C8B9BL,
            0x398F942E01920CF0L,
            0x3B0B51ECBF6DB221L,
            0x42D11A560FC9FBA9L,
            0x43320DC9D2AE0892L,
            0x440E89208F445FB9L,
            0x46C808A4B5841F57L,
            0x49312BDAFB0077D9L,
            0x4A3797B30328202CL,
            0x4BA3E254E758D70DL,
            0x4BF881E49D37F530L,
            0x4DA972745FEB30C1L,
            0x4EF08C90FF16C675L,
            0x4FD10DDC6D13821FL,
            0x527DB6B46CE3BCBCL,
            0x5728504A6D454FFCL,
            0x599B5C1213A099ACL,
            0x5A5BD85C072E5EFEL,
            0x5AB0CB3071AB40D1L,
            0x5D74D3E5B9370476L,
            0x5D92E6DDDE40ED84L,
            0x5F215622FB630753L,
            0x62DB241274397C34L,
            0x63A220E60A17C7B9L,
            0x665C53C311193973L,
            0x6749835432E0F0D2L,
            0x6A47501EBB2AFDB2L,
            0x6FCABF6FA54CAFFFL,
            0x746BD4A53EC195FBL,
            0x74B50BB9260E31FFL,
            0x75CC60F5871D0FD3L,
            0x767A586A5107FEEFL,
            0x7AA7EE3627A19CF3L
    };

    public static boolean check(String typeName) {
        String className = typeName.replace('$', '.');

        final long BASIC = 0xcbf29ce484222325L;
        final long PRIME = 0x100000001b3L;

        long hash = (((((BASIC ^ className.charAt(0))
                * PRIME)
                ^ className.charAt(1))
                * PRIME)
                ^ className.charAt(2))
                * PRIME;
        for (int i = 3; i < className.length(); ++i) {
            hash ^= className.charAt(i);
            hash *= PRIME;
            if (Arrays.binarySearch(denyHashCodes, hash) >= 0) {
                return false;
            }
        }
        return true;
    }
}