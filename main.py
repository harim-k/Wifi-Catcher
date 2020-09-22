# from image_to_string import *
from google_vision_api import *

# for index in range(1,5):
#     file_name = index
#     image_to_string(file_name)

correct_text_ount = 0
wrong_text_count = 0
for index in range(1,18):
    file_name = index

    # convert image to text
    text_list = detect_text(f'{file_name}.jpg')

    print(f'================{index}================')
    print(text_list)
    password_list = get_password(text_list)
    print(f'password:{password_list}')

    # bnw_text = convert_image_to_string(f'bnw/{file_name}_BNW')
    # cut_text = convert_image_to_string(f'cut/{file_name}')
    #cut_bnw_text = convert_image_to_string_BNW(f'cut/{file_name}')
    
    # text = bnw_text
    # print
    
    

#     password = get_password(text)
#     if(len(password) == 0):
#         password = ''
#     else:
#         password = password[0]

#     print(f'predicted password: {password}')

#     if(is_answer(file_name, password) == True):
#         correct_text_ount += 1
#     else:
#         wrong_text_count += 1
    
    
#     print('====================================')
#     print()

# # file_name = input('input file name: ')
# # make_image_to_string(file_name)
# # make_image_to_string_BNW(file_name)

# print(f'correct_text: {correct_text_ount}')
# print(f'wrong_text: {wrong_text_count}')