from fastapi import UploadFile, File


class OcrRequest:
    """
    번호판 인식 요청 스키마
    multipart/form-data 로 이미지 파일을 수신한다.
    """
    def __init__(self, file: UploadFile = File(...)):
        self.file = file
