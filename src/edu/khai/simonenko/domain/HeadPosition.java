package edu.khai.simonenko.domain;

public class HeadPosition {

    private float roll;
    private float yaw;
    private float pitch;

    public HeadPosition(float roll, float yaw, float pitch) {
        this.roll = roll;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
