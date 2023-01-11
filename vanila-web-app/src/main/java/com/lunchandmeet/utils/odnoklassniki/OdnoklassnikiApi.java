package com.lunchandmeet.utils.odnoklassniki;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.Preconditions;

/**
 * Implementation for odnoklassniki.ru
 * http://dev.odnoklassniki.ru/wiki/display/ok/The+OAuth+2.0+Protocol
 */
public class OdnoklassnikiApi extends DefaultApi20 {

    private static final String AUTHORIZE_URL = "http://www.odnoklassniki.ru/oauth/authorize?client_id=%s&response_type=code&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = String.format("%s&scope=%%s", AUTHORIZE_URL);

    @Override
    public String getAccessTokenEndpoint() {
        return "http://api.odnoklassniki.ru/oauth/token.do";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Odnoklassniki.ru does not support OOB");
        if (config.hasScope()) // Appending scope if present
        {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), URLUtils.formURLEncode(config.getCallback()), URLUtils.formURLEncode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL, config.getApiKey(), URLUtils.formURLEncode(config.getCallback()));
        }
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new OdnoklassnikiTokenExtractor();
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new OdnoklassnikiOAuth20ServiceImpl(this, config);
    }
}
