package net.ogify.engine.secure;

import net.ogify.database.BetaKeyController;
import net.ogify.database.entities.BetaKey;
import net.ogify.engine.secure.exceptions.ForbiddenException;
import net.ogify.engine.vkapi.exceptions.VkSideError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by melge on 20.10.2015.
 */
@Component
public class BetaAuthController extends ProductionAuthController {
    @Autowired
    BetaKeyController betaKeyController;

    @Override
    public Long auth(String code, String redirectUrl, String sessionSecret, String betaKey) throws VkSideError {
        BetaKey betaKeyInternal = checkBetaKey(betaKey);

        betaKeyInternal.incrementUsedTime();
        betaKeyController.saveOrUpdate(betaKeyInternal);

        return super.auth(code, redirectUrl, sessionSecret, betaKey);
    }

    public BetaKey checkBetaKey(String key) {
        BetaKey betaKeyInternal = betaKeyController.getByKey(key);
        if(betaKeyInternal == null)
            throw new ForbiddenException("Sorry there is no provided key");
        if(betaKeyInternal.getUsedTime() > 5)
            throw new ForbiddenException("You have used your key too many times");

        return betaKeyInternal;
    }
}
