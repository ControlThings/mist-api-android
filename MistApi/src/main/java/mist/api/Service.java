package mist.api;

import addon.AddonService;
import mist.api.MistApi;

/**
 * Created by jan on 11/7/17.
 */

public class Service extends AddonService {
    public void startAddon() {
        MistApi.getInstance().startMistApi(getBaseContext());
    }
}
