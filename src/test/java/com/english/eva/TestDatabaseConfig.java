package com.english.eva;

import com.english.eva.config.DatabaseConfig;

public class TestDatabaseConfig {

    private static int counter = 0;

    public static DatabaseConfig create() {
        return new DatabaseConfig("mem:test_" + (counter++));
    }
}
