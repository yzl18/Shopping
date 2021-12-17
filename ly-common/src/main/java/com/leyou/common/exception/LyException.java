package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LyException extends RuntimeException {
    //定义了一个枚举对象，并没有new，则就没有使用它的构造函数
    //当别人需要使用时，则需要传一个enum内部的对象
    private ExceptionEnum exceptionEnum;
}
