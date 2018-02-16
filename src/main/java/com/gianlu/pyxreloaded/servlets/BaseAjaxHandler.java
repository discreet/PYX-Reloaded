package com.gianlu.pyxreloaded.servlets;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.handlers.BaseHandler;
import com.gianlu.pyxreloaded.handlers.Handlers;
import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class BaseAjaxHandler extends BaseCahHandler {

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) throws StatusException {
        if (user != null) user.userDidSomething();
        if (op == null || op.isEmpty()) throw new CahException(Consts.ErrorCode.OP_NOT_SPECIFIED);

        BaseHandler handler;
        Class<? extends BaseHandler> cls = Handlers.LIST.get(op);
        if (cls != null) {
            try {
                Constructor<?> constructor = cls.getConstructors()[0];
                Parameter[] parameters = constructor.getParameters();
                Object[] objects = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++)
                    objects[i] = Providers.get(parameters[i].getAnnotations()[0].annotationType()).get();

                handler = (BaseHandler) constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new CahException(Consts.ErrorCode.BAD_OP, ex);
            }
        } else {
            throw new CahException(Consts.ErrorCode.BAD_OP);
        }

        return handler.handle(user, params, exchange).obj();
    }
}