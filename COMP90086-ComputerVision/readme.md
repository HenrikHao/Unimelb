# Stability Prediction with ResNet and Vision Transformer (ViT)

## Overview

This repository contains the implementation and comparative evaluation of two advanced neural network architectures, Residual Networks (ResNet) and Vision Transformers (ViT), for predicting stable heights from images of block structures using the ShapeStacks dataset.

## Authors
- Zhuoyang Hao (Student ID: 1255309)
- Jiani Xie (Student ID: 1025409)

## Objectives
- Evaluate and compare the effectiveness of ResNet and ViT models in predicting stable heights from images.
- Analyze model performance, robustness to occlusions, data imbalance handling, and computational efficiency.

## Dataset
- **ShapeStacks Dataset**
- Annotated images indicating stable height classifications from 1 to 6.
- Train-validation split: 80%-20%

## Project Structure
```
.
├── Project/
│   ├── notebook/
|   ├── predictions/
│   └── readme.md
├── CV_Final_Project_Report.pdf
└── README.md
```

## Model Details
### ResNet
- **ResNet-50** and **ResNet-152** models pre-trained on ImageNet.
- Modified final layers for 6-class classification.
- Early stopping and learning rate reduction.

### Vision Transformer (ViT)
- **ViT-Base-Patch16-224** model pre-trained on ImageNet.
- Utilizes attention mechanism and global contextual information.
- Customized classification layers using the CLS token embedding.

## Preprocessing
- Images resized to (224, 224, 3).
- Normalization across RGB channels (mean: 0.5, std: 0.5).
- No additional augmentation applied.

## Training Strategies
- **Loss function:** Cross Entropy Loss.
- **Optimization:** Adam optimizer with learning rate scheduler (`ReduceLROnPlateau`).
- **Early Stopping:** Patience of 7 epochs.

## Results Summary
| Model               | Validation Accuracy | Validation Loss | Test Accuracy |
|---------------------|---------------------|-----------------|---------------|
| **ResNet-50**       | 0.55                | 1.16            | 0.57          |
| **ResNet-152**      | 0.53                | 1.32            | 0.53          |
| **ViT-Base**        | 0.52                | 1.17            | 0.52          |

- ResNet-50 demonstrated higher accuracy overall, whereas ViT showed robustness against imbalanced data.

## Attention Visualization
- Attention maps generated to visualize important image regions influencing ViT’s predictions.

## Conclusion
- ResNet suitable for well-distributed data; ViT advantageous for imbalanced or complex datasets.
- Future exploration suggested with Swin Transformers to combine strengths of ResNet and ViT.

## References
- Refer to the provided `CV_Final_Project_Report.pdf` for detailed discussions, methodologies, and analysis.

