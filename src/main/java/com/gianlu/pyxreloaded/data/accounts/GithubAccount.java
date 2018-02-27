package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.server.BaseCahHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GithubAccount extends UserAccount {
    public final String id;

    public GithubAccount(ResultSet set) throws BaseCahHandler.CahException, SQLException {
        super(set);

        id = set.getString("github_user_id");
    }

    public GithubAccount(String nickname, GithubProfileInfo info) {
        super(nickname, info.email, Consts.AuthType.GITHUB, info.avatarUrl);

        id = info.id;
    }
}
