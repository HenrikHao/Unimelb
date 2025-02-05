# Repository Structure

The following is a description of the repository structure:
```
├── model/ 
├── notebook/ 
├── predictions/ 
├── test/ 
├── train/ 
├── README.md 
├── test.csv 
└── train.csv
```
Note that ``train/`` and ``test/`` are not included in this repo

# Instructions to Run the Code
To train the ResNet and ViT models, use the following notebooks: `notebook/resnet_train` and `notebook/vit_train`.

To generate predictions on the test set, run `notebook/resnet_test` and `notebook/vit_test`.

For visualizations, execute any notebook starting with `visualization` after completing the model training.