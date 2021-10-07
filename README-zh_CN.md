# 小表情 
## 保持简单
太年轻，太简单，有时还很幼稚
[English](./README.md)

---
> 作者：侯宇轩
> [代码地址](https://github.com/ihewro/Android_Expression_Package)
---
## 简介
使用[ncnn](https://github.com/Tencent/ncnn)自动识别表情包文字，根据表情包文字搜索，表情包分类管理应用

## 特点
+ 保持简单！一个应用只做一件事，就是如何快速检索表情包
+ 本地识别，不上传任何图片到互联网，保证隐私
+ 不储存任何图片，仅保存图片路径，减少闪存占用

## 感谢
+ 代码修改自：[ihewro/Android_Expression_Package](https://github.com/ihewro/Android_Expression_Package)
+ OCR来自：[FeiGeChuanShu/ncnn_paddleocr](https://github.com/FeiGeChuanShu/ncnn_paddleocr)

## TODO list

* [ ] 优化recyclerView 和 ViewPager 在显示大量图片时候的性能
* [ ] 首页头图改成图片轮播，仿造网易云的首页图片轮播
* [ ] 减少manager类的实例化次数
* [ ] 全局扫描图片的时候不会扫描已经保存的图片
* [ ] 点击♥添加图片到我的收藏
* [ ] 修复扫描图片后有的表情描述没有更新
