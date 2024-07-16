package org.test.profiles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("profile1")
public class ProfilesClass1 {
}
