from google_vision_api import *
import os

# depend on your path
#os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "C:/Users/harimkim/Desktop/key.json"
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "/Users/harim/Desktop/key.json"



correct_text_ount = 0
wrong_text_count = 0


def print_result(index, result_name, result):
    print()
    print()
    print()
    print()
    print(f"================================================== result {index} ===========================================================")
    print()
    print(f'{result_name} : {result}')
    print()
    print("=======================================================================================================================")
    print()
    print()
    print()
    print()




for index in range(1,22):
    file_name = f'{index}.jpg'

    # convert image to text
    text_list = detect_text(file_name)
    id_list = get_id(text_list)
    password_list = get_password(text_list)
    # print(f'================{index}================')
    # print(f'text:{text_list}')
    # id_list = get_id(text_list)
    # password_list = get_password(text_list)
    # print(f'id:{id_list}')
    # print(f'password:{password_list}')

    print_result(index, 'text', text_list)
    print_result(index, 'id', id_list)
    print_result(index, 'password', password_list)


