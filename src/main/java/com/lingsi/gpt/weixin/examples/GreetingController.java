package com.lingsi.gpt.weixin.examples;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lingsi.gpt.weixin.pay.weixinservice.CodeState;
import com.lingsi.gpt.weixin.pay.weixinservice.Result;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                        String.format(template, name));
    }

    @PostMapping("/user")
    public Result<UserRequest> createUser(@RequestBody UserRequest userRequest) {
        return new Result<UserRequest>(CodeState.RESULT_CODE_SUCCESS, userRequest);
    }
}