package com.example.studentinfo.service.reference;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class SecurityService {

    private final boolean useRSA = true;
    private final String ALGORITHM = "RSA";
    private final int KEY_SIZE = 1024;

    private KeyPairGenerator gen;
    private Cipher cipher;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public SecurityService() {
        try {
            gen = KeyPairGenerator.getInstance(ALGORITHM);
            gen.initialize(KEY_SIZE);
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Contract(pure = true)
    private static String genRedisKey(@NotNull String uuid) {
        return "key" + uuid;
    }

    public KeyPair generateKeyPair() {
        return gen.generateKeyPair();
    }

    public String saveKeyForBase64(@NotNull Key key) {
        return new String(Base64.getEncoder().encode(key.getEncoded()));
    }

    @Nullable
    private PrivateKey getPrivateKeyFromBase64(@NotNull String priKeyBase64) {
        byte[] encPriKey = Base64.getDecoder().decode(priKeyBase64);
        var encPriKeySpec = new PKCS8EncodedKeySpec(encPriKey);
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(encPriKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private byte[] decrypt(@NotNull byte[] cipherData, @NotNull PrivateKey priKey) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, priKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return cipher.doFinal(cipherData);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createUuidForPrivateKey(@NotNull String privateKey) {
        String uuid;
        Boolean hasKey;
        do {
            uuid = RandomStringUtils.randomAlphanumeric(16);
            hasKey = stringRedisTemplate.hasKey(genRedisKey(uuid));
        } while (hasKey != null && hasKey);
        stringRedisTemplate.opsForValue().set(genRedisKey(uuid), privateKey, 6, TimeUnit.HOURS);
        return uuid;
    }

    private String getPrivateKeyByUuid(@NotNull String uuid) {
        var privateKey = stringRedisTemplate.opsForValue().get(genRedisKey(uuid));
        if (privateKey != null) {
            stringRedisTemplate.delete(uuid);
        }
        return privateKey;
    }

    @Nullable
    public JSONObject decryptDataJSON(@NotNull JSONObject src) {
        var uuid = src.getString("uuid");
        if (uuid == null) {
            return null;
        }
        var data = src.getString("data");
        if (data == null) {
            return null;
        }
        if (!useRSA) {
            return JSONObject.parseObject(data);
        }
        var privateKeyBase64 = getPrivateKeyByUuid(uuid);
        if (privateKeyBase64 == null) {
            return null;
        }
        var privateKey = getPrivateKeyFromBase64(privateKeyBase64);
        if (privateKey == null) {
            return null;
        }
        byte[] b = decrypt(Base64.getDecoder().decode(data), privateKey);
        return b == null ? null : JSONObject.parseObject(new String(b));
    }
}
