package com.lingsi.gpt.weixin.pay.weixinservice;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lingsi.gpt.weixin.examples.UserRequest;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

@RestController
public class PrePayController {

    @PostMapping("/prepay")
    public Result<UserRequest> createUser(@RequestBody UserRequest userRequest) {
        return new Result<UserRequest>(CodeState.RESULT_CODE_SUCCESS, userRequest);
    }


	public static String appId = System.getenv("GPT_PAY_APP_ID");
	/** 商户号 */
	public static String merchantId = System.getenv("GPT_PAY_MERCHANT_ID");
	/** 商户API私钥路径 */
	public static String privateKeyPath = System.getenv("GPT_PAY_PRIVATE_KEY_PATH");
	/** 商户证书序列号 */
	public static String merchantSerialNumber = System.getenv("GPT_PAY_MERCHANT_SERIAL_NUMBER");
	/** 商户APIV3密钥 */
	public static String apiV3key = System.getenv("GPT_PAY_API_V3_KEY");

    @CrossOrigin(origins = "http://localhost:3000")
	@PostMapping("/payPre")
    public Result<PrePay> payPre(UserRequest userRequest) {
		// 使用自动更新平台证书的RSA配置
        // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(merchantId)
                        .privateKeyFromPath(privateKeyPath)
                        .merchantSerialNumber(merchantSerialNumber)
                        .apiV3Key(apiV3key)
                        .build();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(100);
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription("测试商品标题");
        request.setNotifyUrl("https://notify_url");
        request.setOutTradeNo("out_trade_no_001");
        // 调用下单方法，得到应答
        PrepayResponse response = service.prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        PrePay prePay = new PrePay();
        prePay.setCodeUrl(response.getCodeUrl());
        return new Result<PrePay>(CodeState.RESULT_CODE_SUCCESS, prePay);
    }
}