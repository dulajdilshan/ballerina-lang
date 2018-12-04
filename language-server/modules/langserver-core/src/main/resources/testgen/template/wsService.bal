@test:Config
function ${testServiceFunctionName} () {
    http:WebSocketClient ${endpointName} = new(
        ${serviceUriStrName},
        config = {callbackService: ${callbackServiceName}, readyOnConnect: false}
    );
    //Send a message
    _ = ${endpointName}->pushText("hey");
}

service ${callbackServiceName} = @http:WebSocketServiceConfig {} service {
    resource function onText(http:WebSocketClient ${callbackServiceName}Ep, string text) {
        //Test received message
        test:assertEquals(text, "hey", msg = "Received message should be equal to the expected message");
    }
};
