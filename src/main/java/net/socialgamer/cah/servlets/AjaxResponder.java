package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.handlers.BaseHandler;
import net.socialgamer.cah.handlers.Handlers;
import org.jetbrains.annotations.NotNull;

public class AjaxResponder extends CahResponder {
    @Override
    protected JsonElement handleRequest(@NotNull String op, User user) throws StatusException {
        if (user != null) user.userDidSomething();

        final BaseHandler handler;
        Class<? extends BaseHandler> cls = Handlers.LIST.get(op);
        if (cls != null) {
            try {
                handler = cls.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new CahException(Constants.ErrorCode.BAD_OP, ex);
            }
        } else {
            throw new CahException(Constants.ErrorCode.BAD_OP);
        }

        return handler.handle(user);
    }
}
