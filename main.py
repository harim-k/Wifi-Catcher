from google_vision_api import *
import os

# depend on your path
#os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Users/harimkim/Desktop/key.json"
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/Users/harim/Desktop/key.json"



correct_text_ount = 0
wrong_text_count = 0
for index in range(1,18):
    file_name = index

    # convert image to text
    text_list = detect_text(f'{file_name}.jpg')

    print(f'================{index}================')
    print(f'text:{text_list}')
    id_list = get_id(text_list)
    password_list = get_password(text_list)
    print(f'id:{id_list}')
    print(f'password:{password_list}')
