//
// The Trigger is a Java class that is an emulator of a trigger.
//

package com.lenovo.billing.smartequipment;

public class Trigger {

    private boolean value;
    private String name;

    public Trigger(String name) {
        this.value = false;
        this.name = name;
    }

    public void set() {
        System.out.format("The %s is triggered.\n", name);
        this.value = true;
    }

    public void clear() {
        System.out.format("Clear the trigger %s.\n", name);
        this.value = false;
    }

    public boolean flag() {
        return this.value;
    }

}