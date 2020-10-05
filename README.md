# Wifi-Catcher


Wifi-Catcher는 스마트폰 와이파이 자동 연결 서비스입니다.

스마트폰으로 와이파이 정보를 사진으로 찍으면 네트워크 이름과 비밀번호를 OCR(Optical Character Recognition) 기술을 통해 인식하여 해당 와이파이에 자동으로 연결되도록 도와줍니다.



#android application
#OCR
#google vision
#java
#python

2. Wifi-Catcher

- 설명


Wifi-Catcher는 스마트폰 와이파이 자동 연결 서비스입니다. 스마트폰으로 와이파이 정보를 사진
으로 찍으면 네트워크 이름과 비밀번호를 OCR(Optical Character Recognition) 기술을 통해 인식하
여 해당 와이파이에 자동으로 연결되도록 도와줍니다.


- 구현


언어 : java, python
API : google vision


안드로이드 어플리케이션 형태로 구현하였으며 동작은 다음과 같습니다.
스마트폰으로 와이파이 사진을 찍으면, google vision을 사용하여 사진에서 모든 텍스트 추출하고,
추출한 텍스트에서 ‘와이파이 정보 추출 알고리즘’을 적용하여 와이파이 ssid와 비밀번호를 추출하여, 와이파이에 연결합니다.
