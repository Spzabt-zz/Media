import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

let stompClient = null
//const handlers = []
let handlerMessage = function () {}

export function connect() {
    const socket = () => new SockJS('/gs-guide-websocket')
    console.log('initial connect!!! ')
    stompClient = Stomp.over(socket)
    stompClient.debug = () => {}
    stompClient.connect({}, frame => {
        stompClient.subscribe('/topic/activity', message => {
            handlerMessage(JSON.parse(message.body))
            console.log('sent to subscribe !!!' + message.body)
            //handlers.forEach(handler => handler(JSON.parse(message.body)))
            /*
            handlers.forEach((handler) => {
                handler(JSON.parse(message.body))
            })*/
        })
    })
}

export function addHandler(handler) {
    //handlers.push(handler)
    handlerMessage = handler
    console.log('handler called!!!')
}

export function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect()
    }
    console.log("Disconnected")
}

export function sendMessage(message) {
    stompClient.send("/app/changeMessage", {}, JSON.stringify(message))
}