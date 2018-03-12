package com.ue.moon.entity;

import com.ue.resource.util.GsonHolder;

/**
 * Created by hawk on 2016/12/13.
 */

public class MoonPoint {
    public float x;
    public float y;
    public int f;
    public int c;

    public MoonPoint(float x, float y, int f, int c) {
        this.x = x;
        this.y = y;
        this.f = f;
        this.c = c;
    }

    @Override
    public int hashCode() {
        return f;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoonPoint moonPoint = (MoonPoint) o;
        if (x != moonPoint.x) return false;
        if (y != moonPoint.y) return false;
        return true;
    }

    @Override
    public String toString() {
        return GsonHolder.getGson().toJson(this);
    }
}
