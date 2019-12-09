package com.slicejobs.algsdk.algtasklibrary.utils;

import com.squareup.otto.Bus;

/**
 * Created by jgzhu on 10/9/14.
 */
public class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {}
}
