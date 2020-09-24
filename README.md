# add_to_gallery
# 图片加入图库适配
*Why？*
###### android碎片化，同存的android系统版本过多，从android5.0到现在的android11，导致一些功能适配变得困难。本代码库是为适配图片加入系统相册功能而建。主要代码见 ImagePopupUtils.saveBmpToAlbum()方法，核心代码如下：
```
if (AppUtil.isAndroidQ()) {
    Uri uri1 = Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
    ContentValues values = new ContentValues();
    long dateToken = System.currentTimeMillis();
    values.put(Media.DATE_TAKEN, dateToken);
    values.put(Media.DESCRIPTION, "this is an image");
    values.put(Media.IS_PRIVATE, 1);
    values.put(Media.DISPLAY_NAME, target.getName());
    values.put(Media.MIME_TYPE, "image/" + ext);
    values.put(Media.TITLE, "image");
    values.put(Media.RELATIVE_PATH, "Pictures");
    long dateAdded = System.currentTimeMillis() / 1000;
    values.put(Media.DATE_ADDED, dateAdded);
    values.put(Media.DATE_MODIFIED, dateAdded);
    
    Uri insertUri = context.getContentResolver().insert(uri1, values);
    OutputStream os = context.getContentResolver().openOutputStream(insertUri);
    final Bitmap bitmap = BitmapFactory.decodeFile(target.getAbsolutePath());
    bitmap.compress(CompressFormat.JPEG, 90, os);
    os.close();
} else {
    try {
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                target.getAbsolutePath(), target.getName(), null);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(target.getAbsolutePath())));
}
mainHandler.post(new Runnable() {
    @Override
    public void run() {
        Toast.makeText(context, "已保存到图库！", Toast.LENGTH_SHORT).show();
    }
});
```

*说明：主要区分android9（android Q）及以上，android9以下。*
