/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
dojo.require("dojox.cometd");

var room = {
    _last: "",
    _userName: null,
    _connected: true,

    join: function(name){

        if(name == null || name.length==0 ){
            alert('Please enter a userName!');
        }else{
            var url=new String(document.location).replace(/http:\/\/[^\/]*/,'').replace(/\/chat\/.*$/,'')+"/bayeux";
            dojox.cometd.init(url);
            this._connected=true;

            this._userName=name;
            dojo.byId('join').className='hidden';
            dojo.byId('joined').className='';
            dojo.byId('chatText').focus();

            // subscribe and join
            dojox.cometd.startBatch();
            dojox.cometd.subscribe("/chat/room0", room, "_chat");
            dojox.cometd.publish("/chat/room0", {
                user: room._userName,
                join: true,
                chat : room._userName+" has joined"
            });
            dojox.cometd.endBatch();

            // handle cometd failures while in the room
            room._meta=dojo.subscribe("/cometd/meta",dojo.hitch(this,function(event){
                
                if (event.action=="handshake") {
                    room._chat({
                        data:{
                            join:true,
                            user:"SERVER",
                            chat:"reinitialized client id: "+event.response.clientId
                        }
                    });
                    dojox.cometd.subscribe("/chat/room0", room, "_chat");
                } else if (event.action=="connect") {
                    if (event.successful && !this._connected)
                        room._chat({
                            data:{
                                leave:true,
                                user:"SERVER",
                                chat:"reconnected!"
                            }
                        });
                    if (!event.successful && this._connected){
                        room._chat({
                            data:{
                                leave:true,
                                user:"SERVER",
                                chat:"disconnected!"
                            }
                        });
                    }
                    this._connected=event.successful;
                }
            }));
        }
    },

    leave: function(){
        if (room._userName==null)
            return;

        if (room._meta)
            dojo.unsubscribe(room._meta);
        room._meta=null;

        dojox.cometd.startBatch();
        dojox.cometd.unsubscribe("/chat/room0", room, "_chat");
        dojox.cometd.publish("/chat/room0", {
            user: room._userName,
            leave: true,
            chat : room._userName+" has left"
        });
        dojox.cometd.endBatch();

        // switch the input form
        dojo.byId('join').className='';
        dojo.byId('joined').className='hidden';
        dojo.byId('userName').focus();
        room._userName=null;
        dojox.cometd.disconnect();
    },

    chat: function(text){
        if(!text || !text.length){
            return ;
        }
        dojox.cometd.publish("/chat/room0", {
            user: room._userName,
            chat: text
        });
    },

    _chat: function(message){
        var chat=dojo.byId('messageLog');
        if(!message.data){
            alert("bad message format "+message);
            return;
        }
        var from=message.data.user;
        var special=message.data.join || message.data.leave;
        var text=message.data.chat;
        if(!text){
            return;
        }

        if( !special && from == room._last ){
            from="...";
        }else{
            room._last=from;
            from+=":";
        }

        if(special){
            chat.innerHTML += "<span class=\"alert\"><span class=\"from\">"+from+" </span><span class=\"text\">"+text+"</span></span><br/>";
            room._last="";
        }else{
            chat.innerHTML += "<span class=\"from\">"+from+" </span><span class=\"text\">"+text+"</span><br/>";
        }
        chat.scrollTop = chat.scrollHeight - chat.clientHeight;
    },

    _init: function(){
        dojo.byId('join').className='';
        dojo.byId('joined').className='hidden';
        dojo.byId('userName').focus();

        var element=dojo.byId('userName');
        element.setAttribute("autocomplete","OFF");
        dojo.connect(element, "onkeyup", function(e){
            if(e.keyCode == dojo.keys.ENTER){
                room.join(dojo.byId('userName').value);
                return false;
            }
            return true;
        });

        element=dojo.byId('joinB');
        element.onclick = function(){
            room.join(dojo.byId('userName').value);
            return false;
        }

        element=dojo.byId('chatText');
        element.setAttribute("autocomplete","OFF");
        dojo.connect(element, "onkeyup", function(e){
            if(e.keyCode == dojo.keys.ENTER){
                room.chat(dojo.byId('chatText').value);
                dojo.byId('chatText').value='';
                return false;
            }
            return true;
        });

        element=dojo.byId('sendB');
        element.onclick = function(){
            room.chat(dojo.byId('chatText').value);
            dojo.byId('chatText').value='';
        }

        element=dojo.byId('leaveB');
        element.onclick = function(){
            room.leave();
        }
    }
};

dojo.addOnLoad(room, "_init");
dojo.addOnUnload(room,"leave");
