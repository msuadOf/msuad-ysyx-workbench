# "一生一芯"工程项目

这是"一生一芯"的工程项目. 通过运行
```bash
bash init.sh subproject-name
```
进行初始化, 具体请参考[实验讲义][lecture note].

[lecture note]: https://ysyx.oscc.cc/docs/

运行以克隆所有分支
```bash
git clone --bare git@github.com:msuadOf/msuad-ysyx-workbench.git
```
或
```bash
git clone git@github.com:msuadOf/msuad-ysyx-workbench.git
git fetch -all
```

运行以上传所有分支
```bash
 git push --mirror
```
或
```bash
git push --all origin -u
git push
```

Rename Branch name
```shell
git branch -m main master
git fetch origin
git branch -u origin/master master
git remote set-head origin -a
```
