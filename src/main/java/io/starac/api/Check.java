package io.starac.api;

import io.starac.data.PlayerData;

public interface Check {

    String getName();

    CheckCategory getCategory();

    CheckResult handle(PlayerData data);

}