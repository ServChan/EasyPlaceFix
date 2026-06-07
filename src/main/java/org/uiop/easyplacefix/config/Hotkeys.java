package org.uiop.easyplacefix.config;

import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;

import static org.uiop.easyplacefix.config.easyPlacefixConfig.*;

public class Hotkeys {

    public static void init(){
        InputEventHandler.getKeybindManager().registerKeybindProvider(new IKeybindProvider() {
            @Override
            public void addKeysToMap(IKeybindManager iKeybindManager) {
                iKeybindManager.addKeybindToMap(OBSERVER_DETECT.getKeybind());
                iKeybindManager.addKeybindToMap(ENABLE_FIX.getKeybind());
                iKeybindManager.addKeybindToMap(IGNORE_NBT.getKeybind());
                iKeybindManager.addKeybindToMap(LOOSEN_MODE.getKeybind());
                iKeybindManager.addKeybindToMap(Allow_Interaction.getKeybind());
                iKeybindManager.addKeybindToMap(CLIENT_ROTATION_REVERT.getKeybind());
                iKeybindManager.addKeybindToMap(DIAGNOSTIC_STATUS.getKeybind());
            }

            @Override
            public void addHotkeys(IKeybindManager iKeybindManager) {

            }
        });
    }
}
