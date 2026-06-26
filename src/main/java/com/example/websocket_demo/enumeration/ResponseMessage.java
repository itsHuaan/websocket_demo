package com.example.websocket_demo.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ResponseMessage {
    FETCH_ALL_ADMINISTRATIVE_REGIONS("fetch.all.administrative.regions"),
    CANNOT_FIND_ADMINISTRATIVE_REGIONS("empty.administrative.regions"),
    CANNOT_FIND_ADMINISTRATIVE_REGION("empty.administrative.region"),
    FETCH_ALL_ADMINISTRATIVE_UNITS("fetch.all.administrative.units"),
    CANNOT_FIND_ADMINISTRATIVE_UNITS("empty.administrative.units"),
    CANNOT_FIND_ADMINISTRATIVE_UNIT("empty.administrative.unit"),
    OTP_GENERATED("otp.generated"),
    FILL_ALL_FIELDS("fill.all.fields"),
    NEED_MEDIA_ITEM("need.media.item"),
    USER_NOT_FOUND("user.not.found"),
    FAILED_TO_UPLOAD_MEDIA("failed.to.upload.media"),
    PRODUCT_NOT_FOUND("product.not.found"),
    NEED_SKU_WITH_PRICE("need.sku.with.price"),
    OPTION_NOT_FOUND("option.not.found"),
    OPTION_VALUE_NOT_FOUND("option.value.not.found"),
    ROLE_NAME_EMPTY("role.name.empty"),
    ROLE_EXISTS("role.exists"),
    ROLE_NOT_FOUND("role.not.found"),
    USER_IDENTIFIER_NOT_FOUND("user.identifier.not.found"),
    USER_NULL("user.null"),
    USERNAME_NULL("username.null"),
    USERNAME_EXISTS("username.exists"),
    EMAIL_EXISTS("email.exists"),
    PASSWORD_NULL("password.null"),
    OTP_USED("otp.used"),
    OTP_INVALID_OR_EXPIRED("otp.invalid.or.expired"),

    ;
    String code;
}
