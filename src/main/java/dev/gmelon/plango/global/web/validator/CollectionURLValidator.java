package dev.gmelon.plango.global.web.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CollectionURLValidator implements ConstraintValidator<CollectionURLValidation, Collection<? extends CharSequence>> {

    private String protocol;
    private String host;
    private int port;

    @Override
    public void initialize(CollectionURLValidation annotation) {
        this.protocol = annotation.protocol();
        this.host = annotation.host();
        this.port = annotation.port();
    }

    @Override
    public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }

        for (CharSequence value : values) {
            if (!isValid(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValid(CharSequence value) {
        if ( value == null || value.length() == 0 ) {
            return true;
        }

        URL url;
        try {
            url = new URL( value.toString() );
        }
        catch (MalformedURLException e) {
            return false;
        }

        if ( protocol != null && protocol.length() > 0 && !url.getProtocol().equals( protocol ) ) {
            return false;
        }

        if ( host != null && host.length() > 0 && !url.getHost().equals( host ) ) {
            return false;
        }

        if ( port != -1 && url.getPort() != port ) {
            return false;
        }

        return true;
    }
}
