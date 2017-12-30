/**
 * Copyright (c) 2012, Andy Janata
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.socialgamer.cah.handlers;

import com.google.inject.Inject;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.RequestWrapper;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;


/**
 * BaseHandler for chat messages.
 *
 * @author Andy Janata (ajanata@socialgamer.net)
 */
public class ChatHandler extends BaseHandler {

    public static final String OP = AjaxOperation.CHAT.toString();

    private final ConnectedUsers users;

    @Inject
    public ChatHandler(final ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public Map<ReturnableData, Object> handle(final RequestWrapper request,
                                              final HttpSession session) {
        final Map<ReturnableData, Object> data = new HashMap<ReturnableData, Object>();

        final User user = (User) session.getAttribute(SessionAttribute.USER);
        assert (user != null);
        final boolean wall = request.getParameter(AjaxRequest.WALL) != null
                && Boolean.valueOf(request.getParameter(AjaxRequest.WALL));
        final boolean emote = request.getParameter(AjaxRequest.EMOTE) != null
                && Boolean.valueOf(request.getParameter(AjaxRequest.EMOTE));

        if (request.getParameter(AjaxRequest.MESSAGE) == null) {
            return error(ErrorCode.NO_MSG_SPECIFIED);
        } else if (/* wall && */!user.isAdmin()) {
            // Making global chat admin-only because it's hopeless
            return error(ErrorCode.NOT_ADMIN);
        } else {
            final String message = request.getParameter(AjaxRequest.MESSAGE).trim();

            // Intentionally leaving flood protection as per-user, rather than
            // changing it to per-user-per-game.
            if (user.getLastMessageTimes().size() >= Constants.CHAT_FLOOD_MESSAGE_COUNT) {
                final Long head = user.getLastMessageTimes().get(0);
                if (System.currentTimeMillis() - head < Constants.CHAT_FLOOD_TIME) {
                    return error(ErrorCode.TOO_FAST);
                }
                user.getLastMessageTimes().remove(0);
            }

            if (message.length() > Constants.CHAT_MAX_LENGTH) {
                return error(ErrorCode.MESSAGE_TOO_LONG);
            } else if (message.length() == 0) {
                return error(ErrorCode.NO_MSG_SPECIFIED);
            } else {
                user.getLastMessageTimes().add(System.currentTimeMillis());
                final HashMap<ReturnableData, Object> broadcastData = new HashMap<ReturnableData, Object>();
                broadcastData.put(LongPollResponse.EVENT, LongPollEvent.CHAT.toString());
                broadcastData.put(LongPollResponse.FROM, user.getNickname());
                broadcastData.put(LongPollResponse.MESSAGE, message);
                if (user.isAdmin()) {
                    broadcastData.put(LongPollResponse.FROM_ADMIN, true);
                }
                if (wall) {
                    broadcastData.put(LongPollResponse.WALL, true);
                }
                if (emote) {
                    broadcastData.put(LongPollResponse.EMOTE, true);
                }
                users.broadcastToAll(MessageType.CHAT, broadcastData);
            }
        }

        return data;
    }
}
