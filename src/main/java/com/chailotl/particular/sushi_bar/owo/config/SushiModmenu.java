package com.chailotl.particular.sushi_bar.owo.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SushiModmenu
{
	String modId();
	String uiModelId() default "owo:config";
}