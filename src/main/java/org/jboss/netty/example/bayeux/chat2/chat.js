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
    _userName: null,
    _connected: true,

    join: function(name){

        if(name == null || name.length==0 ){
            alert('Please enter a userName!');
        }else{
            var url=new String(document.location).replace(/http:\/\/[^\/]*/,'').replace(/\/chat\d?\/.*$/,'')+"/bayeux";
            dojox.cometd.init(url);
            this._connected=true;

            this._userName=name;
            dojo.byId('join').className='hidden';
            dojo.byId('joined').className='';
            dojo.byId('chatText').focus();

            // subscribe and join
            dojox.cometd.startBatch();
            dojox.cometd.subscribe("/chat/room0/public", room, "_chat");
            dojox.cometd.subscribe("/chat/room0/"+name, room, "_chat");
            dojox.cometd.publish("/chat/room0/public", {
                from: room._userName,
                type: "join"
            });
            dojox.cometd.endBatch();

            // handle cometd failures while in the room
            room._meta=dojo.subscribe("/cometd/meta",dojo.hitch(this,function(event){
                
                if (event.action=="handshake") {
                    if(event.successful){
                        room._chat({
                            data:{
                                from:"server",
                                chat:"reinitialized client id: "+event.response.clientId
                            }
                        });
                    }else{
                        room._chat({
                            data:{
                                from:"server",
                                chat:event.response.error
                            }
                        });
                    }
                //dojox.cometd.subscribe("/chat/room0", room, "_chat");
                } else if (event.action=="connect") {
                    if (event.successful && !this._connected)
                        room._chat({
                            data:{
                                from:"server",
                                chat:"reconnected!"
                            }
                        });
                    if (!event.successful && this._connected){
                        room._chat({
                            data:{
                                from:"server",
                                chat:"disconnected!"
                            }
                        });
                        room.leave();
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
        dojox.cometd.unsubscribe("/chat/room0/public", room, "_chat");
        dojox.cometd.publish("/chat/room0/public", {
            from: room._userName,
            type: "leave",
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
            alert("Please enter some message");
            return ;
        }
        if(dojo.byId('privateC').checked){
            var dest=dojo.byId('destName').value;
            if(!dest||!dest.length){
                alert("Please enter the destination username");
                return;
            }
            var data={
                from: room._userName,
                chat: text,
                to:dest
            };
            if( text.charAt(0)!="/"){
                room._chat({
                    data:data
                });
            }
            dojox.cometd.publish("/chat/room0/"+dest, data);
            
        }else{
            dojox.cometd.publish("/chat/room0/public", {
                from: room._userName,
                chat: text
            });
        }
        
    },

    _chat: function(message){
        var chat=dojo.byId('messageLog');
        if(!message.data){
            alert("bad message format "+message);
            return;
        }
        var from=message.data.from;
        var to=message.data.to;
        var text=null;
        if(message.data.type=="join"){
            text=from+" has joined.";
        }else if(message.data.type=="leave"){
            text=from+" has left.";
        }else {
            text=message.data.chat;
        }

        if(!text){
            return;
        }

        var title=from;
        if(to){
            title+=" to "+to;
        }
        title+=":";

        chat.innerHTML += "<span class=\"from\">"+title+" </span><span class=\"text\">"+text+"</span><br/>";
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

        element=dojo.byId('privateC');
        element.onclick=function(){
            if(dojo.byId('privateC').checked){
                dojo.byId('destSpan').className='';
            }else{
                dojo.byId('destSpan').className='hidden';
            }
        }

        element=dojo.byId('leaveB');
        element.onclick = function(){
            room.leave();
        }
    }
};

dojo.addOnLoad(room, "_init");
dojo.addOnUnload(room,"leave");
