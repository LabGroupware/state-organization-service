package org.cresplanex.api.state.organizationservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserProfileExistValidateCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserProfileServiceProxy {

    public final CommandEndpoint<UserProfileExistValidateCommand> userProfileExistValidate
            = CommandEndpointBuilder
            .forCommand(UserProfileExistValidateCommand.class)
            .withChannel(SagaCommandChannel.USER_PROFILE)
            .withCommandType(UserProfileExistValidateCommand.TYPE)
            .build();
}
