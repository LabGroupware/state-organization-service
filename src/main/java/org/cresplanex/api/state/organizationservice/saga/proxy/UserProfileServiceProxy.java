package org.cresplanex.api.state.organizationservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserExistValidateCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserProfileServiceProxy {

    public final CommandEndpoint<UserExistValidateCommand> userExistValidate
            = CommandEndpointBuilder
            .forCommand(UserExistValidateCommand.class)
            .withChannel(SagaCommandChannel.USER_PROFILE)
            .withCommandType(UserExistValidateCommand.TYPE)
            .build();
}
