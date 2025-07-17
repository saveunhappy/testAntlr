package com.example.dsl.runtime;

@FunctionalInterface
public interface DslFunction {
    Object execute(Object... args);
} 