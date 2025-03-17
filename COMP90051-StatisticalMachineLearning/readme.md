# Modeling Temporal Dynamics in User Rating Sequences

### Authors:
- Zhuoyang Hao (1255309)
- Keith Howen (1214132)
- Zilin Su (1155122)

## Overview
This project explores how the temporal dynamics of user ratings impact user preferences using the Yahoo Music dataset. The goal is to predict future user song ratings categorized into three classes: Low, Medium, and High. Three machine learning models were evaluated:

- **Long Short-term Memory Networks (LSTM)**
- **Neural Additive Models (NAM)**
- **Transformer-based Models**

## Dataset
The dataset used is the [Yahoo Music dataset](https://webscope.sandbox.yahoo.com/catalog.php?datatype=r&guccounter=1&guce_referrer=aHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS8&guce_referrer_sig=AQAAAGD_X3P2inOyf2vpe5NmqOi5Ak8pSgE8UtFADDYzWidrPKhA0jihPBlNEawjlTXpP5hQxOCRL7ebMpkbbRmkCZlvyPWLAGiliiGDR87ENgKKgLhvk4QMUMUNjz1i1LRLG1x5--phsLQCzTvNu84MIIc8hlpevzFowv_KxdXbrYcc), featuring user interactions with musical items such as songs, albums, artists, and genres. Each interaction record includes:
- User ID
- Item ID
- Rating Score
- Timestamp

## Project Structure
```
project/
├── notebooks/
│   ├── preprocessing.ipynb
│   ├── LSTM.ipynb
│   ├── NAM.ipynb
│   └── transformer.ipynb
├── COMP90051_Assignment3_Report.pdf
└── README.md
```

## Feature Engineering and Preprocessing
Key preprocessing steps included:
- Concatenating training and validation sets based on user ID.
- Transforming ratings into discrete categories:
  - High (rating > 70)
  - Medium (30 ≤ rating ≤ 70)
  - Low (rating < 30)
- Creating binary indicator columns (`isTrack`, `isArtist`, `isAlbum`, `isGenre`).
- Introducing a `timestep` feature to capture the chronological order of user ratings.
- Filtering users by entropy to ensure variability in rating patterns.

## Models
### 1. Long Short-term Memory (LSTM)
Captures sequential patterns and contextual dependencies.
- Components: Embedding layer → LSTM → Fully connected layer.
- Sequence padding and masking used for efficient training.

### 2. Neural Additive Models (NAM)
Interpretable model focusing on additive feature contributions.
- Components: Feature-specific neural networks → Fully connected layers → Softmax classifier.

### 3. Transformer-based Model
Leverages self-attention mechanisms to model global dependencies.
- Components: Embedding with positional encoding → Transformer encoder → Fully connected layer.

## Cross-Validation
Nested cross-validation was implemented:
- Outer loop: Evaluates model performance (20 folds).
- Inner loop: Hyperparameter tuning (5 folds).
- Early stopping used to prevent overfitting (patience = 3 epochs).

## Results
| Model        | Accuracy | Precision (%) | Recall | F1-score |
|--------------|----------|---------------|--------|----------|
| LSTM         | 0.5083   | 41.60±2.20    | 0.4255 | 0.3870   |
| NAM          | 0.4924   | 43.01±1.70    | 0.4184 | 0.3860   |
| Transformer  | 0.4935   | 27.87±4.15    | 0.3736 | 0.2980   |

- **LSTM** performed best, effectively capturing temporal dynamics.
- **Transformer** model struggled to leverage its full capability on locally dependent data.
- **NAM** showed balanced performance but struggled due to its simpler temporal modeling.

## Conclusion
The analysis clearly demonstrates the significance of temporal dynamics in predicting user preferences. The LSTM model's strong performance supports the hypothesis that modeling sequential information substantially improves prediction accuracy.

## References
See detailed citations and methodology in the included [COMP90051_Assignment3_Report.pdf](COMP90051_Assignment3_Report.pdf).

