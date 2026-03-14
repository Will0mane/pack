package me.will0mane.software.pack.api.arch;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides a self-signed {@link SSLContext} that works out of the box with no external configuration.
 * <p>
 * A 2048-bit RSA key pair and self-signed X.509 certificate are generated once on first access
 * and reused for the lifetime of the JVM. The certificate is built using raw DER encoding with
 * only public JDK APIs — no internal {@code sun.*} dependencies or JVM flags required.
 * <p>
 * The built-in trust manager accepts all certificates, making this suitable for internal
 * service-to-service communication.
 * <p>
 * For production use with CA-signed certificates, configure the standard JVM system properties
 * ({@code javax.net.ssl.keyStore}, {@code javax.net.ssl.trustStore}, etc.) and use the default
 * factories directly instead of this class.
 */
public class SslContext {

    private static volatile SSLContext context;

    private SslContext() {
    }

    /**
     * Returns the shared {@link SSLContext} with an auto-generated self-signed certificate.
     */
    public static SSLContext get() {
        if (context == null) {
            synchronized (SslContext.class) {
                if (context == null) {
                    context = create();
                }
            }
        }
        return context;
    }

    /**
     * Returns an {@link SSLSocketFactory} backed by the auto-generated certificate.
     */
    public static SSLSocketFactory socketFactory() {
        return get().getSocketFactory();
    }

    /**
     * Returns an {@link SSLServerSocketFactory} backed by the auto-generated certificate.
     */
    public static SSLServerSocketFactory serverSocketFactory() {
        return get().getServerSocketFactory();
    }

    private static SSLContext create() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            byte[] certDer = buildSelfSignedCertificate(pair);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certDer));

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry("pack", pair.getPrivate(), new char[0], new java.security.cert.Certificate[]{cert});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, new char[0]);

            TrustManager[] trustAll = {new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustAll, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    // --- Self-signed certificate generation via raw DER encoding ---

    // SHA256withRSA algorithm OID: 1.2.840.113549.1.1.11
    private static final byte[] OID_SHA256_RSA = {
            0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x0B
    };

    // commonName attribute OID: 2.5.4.3
    private static final byte[] OID_CN = {0x55, 0x04, 0x03};

    private static byte[] buildSelfSignedCertificate(KeyPair pair) throws Exception {
        byte[] name = derSequence(derSet(derSequence(derOid(OID_CN), derUtf8("pack"))));

        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + 365L * 24 * 60 * 60 * 1000);
        byte[] validity = derSequence(derUtcTime(notBefore), derUtcTime(notAfter));

        byte[] algorithmId = derSequence(derOid(OID_SHA256_RSA), derNull());

        byte[] tbsCertificate = derSequence(
                derExplicit(0, derInteger(BigInteger.valueOf(2))),   // version v3
                derInteger(new BigInteger(64, new SecureRandom())), // serialNumber
                algorithmId,                                         // signature algorithm
                name,                                                // issuer
                validity,                                            // validity
                name,                                                // subject
                pair.getPublic().getEncoded()                        // subjectPublicKeyInfo (already DER)
        );

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(pair.getPrivate());
        sig.update(tbsCertificate);
        byte[] signature = sig.sign();

        return derSequence(tbsCertificate, algorithmId, derBitString(signature));
    }

    // --- ASN.1 DER encoding helpers ---

    private static byte[] derSequence(byte[]... items) {
        return derTagged(0x30, concat(items));
    }

    private static byte[] derSet(byte[]... items) {
        return derTagged(0x31, concat(items));
    }

    private static byte[] derInteger(BigInteger value) {
        return derTagged(0x02, value.toByteArray());
    }

    private static byte[] derBitString(byte[] data) {
        byte[] content = new byte[data.length + 1];
        content[0] = 0; // no unused bits
        System.arraycopy(data, 0, content, 1, data.length);
        return derTagged(0x03, content);
    }

    private static byte[] derOid(byte[] oidBytes) {
        return derTagged(0x06, oidBytes);
    }

    private static byte[] derNull() {
        return new byte[]{0x05, 0x00};
    }

    private static byte[] derUtf8(String s) {
        return derTagged(0x0C, s.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] derUtcTime(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMddHHmmss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return derTagged(0x17, fmt.format(date).getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] derExplicit(int tag, byte[] content) {
        return derTagged(0xA0 | tag, content);
    }

    private static byte[] derTagged(int tag, byte[] content) {
        byte[] len = derLength(content.length);
        byte[] result = new byte[1 + len.length + content.length];
        result[0] = (byte) tag;
        System.arraycopy(len, 0, result, 1, len.length);
        System.arraycopy(content, 0, result, 1 + len.length, content.length);
        return result;
    }

    private static byte[] derLength(int length) {
        if (length < 128) {
            return new byte[]{(byte) length};
        } else if (length < 256) {
            return new byte[]{(byte) 0x81, (byte) length};
        } else if (length < 65536) {
            return new byte[]{(byte) 0x82, (byte) (length >> 8), (byte) length};
        } else {
            return new byte[]{(byte) 0x83, (byte) (length >> 16), (byte) (length >> 8), (byte) length};
        }
    }

    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] a : arrays) {
            out.write(a, 0, a.length);
        }
        return out.toByteArray();
    }
}
